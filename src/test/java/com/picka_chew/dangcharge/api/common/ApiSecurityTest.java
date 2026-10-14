package com.picka_chew.dangcharge.api.common;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.picka_chew.dangcharge.api.admin.dto.StationSyncResponse;
import com.picka_chew.dangcharge.api.user.dto.UserResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/** 인증이 필요한 API와 필요 없는 API가 의도대로 나뉘는지, 오류가 공통 포맷으로 나가는지 확인한다 */
class ApiSecurityTest extends ControllerTestSupport {

	@ParameterizedTest
	@CsvSource({
			"/api/v1/vehicle-models",
			"/api/v1/stations?lat=37.5&lng=127.0&radius=1000",
			"/api/v1/stations/search?keyword=강남",
			"/api/v1/stations/1",
			"/api/v1/stations/1/chargers"
	})
	void publicEndpointsDoNotNeedLogin(String uri) throws Exception {
		mockMvc.perform(get(uri)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
	}

	@ParameterizedTest
	@CsvSource({
			"GET,    /api/v1/users/me",
			"GET,    /api/v1/stations/1/availability?date=2026-10-14",
			"GET,    /api/v1/reservations/1",
			"DELETE, /api/v1/reservations/1",
			"POST,   /api/v1/charging/1/stop",
			"GET,    /api/v1/charging/history",
			"GET,    /api/v1/charging/history/1",
			"POST,   /api/v1/auth/logout"
	})
	void protectedEndpointsRejectMissingToken(String method, String uri) throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.request(org.springframework.http.HttpMethod.valueOf(method), uri))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));
	}

	@Test
	void accessTokenIdentifiesUser() throws Exception {
		given(getMyInfoUseCase.execute(7L)).willReturn(new UserResponse(7L, "driver", "운전자", VehicleModel.TESLA));

		mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.loginId").value("driver"));
	}

	@Test
	void refreshTokenCannotBeUsedAsAccessToken() throws Exception {
		mockMvc.perform(get("/api/v1/users/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenIssuer.issueRefreshToken(7L)))
				.andExpect(status().isUnauthorized());
		verifyNoInteractions(getMyInfoUseCase);
	}

	@Test
	void loggedOutTokenIsRejected() throws Exception {
		given(accessTokenBlacklist.contains(anyString())).willReturn(true);

		mockMvc.perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminApiNeedsAdminKeyNotUserToken() throws Exception {
		given(syncStationsUseCase.execute()).willReturn(new StationSyncResult(1, 2, List.of()));

		mockMvc.perform(post("/api/v1/admin/stations/sync").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/api/v1/admin/stations/sync").header("X-Admin-Key", "wrong"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(post("/api/v1/admin/stations/sync").header("X-Admin-Key", ADMIN_KEY))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.created").value(1))
				.andExpect(jsonPath("$.data.updated").value(2));
	}

	@Test
	void invalidRequestBodyIsBadRequestInCommonFormat() throws Exception {
		mockMvc.perform(post("/api/v1/auth/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"loginId\":\"ab\",\"username\":\"운전자\",\"password\":\"abc12345\",\"vehicleModel\":\"TESLA\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("BAD_REQUEST"));
		verifyNoInteractions(signUpUseCase);
	}

	@Test
	void businessErrorUsesItsStatus() throws Exception {
		given(createReservationUseCase.execute(anyLong(), any(), isNull()))
				.willThrow(new GeneralException(ErrorStatus.RESERVATION_CONFLICT));

		mockMvc.perform(post("/api/v1/reservations")
						.header(HttpHeaders.AUTHORIZATION, bearer(7L))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"chargerId": 1203, "vehicleId": 5,
								"startAt": "2026-10-14T14:00:00+09:00", "endAt": "2026-10-14T15:00:00+09:00"}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("RESERVATION_CONFLICT"));
	}

	@Test
	void pageSizeOutOfRangeIsBadRequestInCommonFormat() throws Exception {
		mockMvc.perform(get("/api/v1/charging/history?size=0").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false));
	}

	@Test
	void reservationRequestCarriesIdempotencyKey() throws Exception {
		mockMvc.perform(post("/api/v1/reservations")
						.header(HttpHeaders.AUTHORIZATION, bearer(7L))
						.header("Idempotency-Key", "key-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"chargerId": 1203,
								"startAt": "2026-10-14T14:00:00+09:00", "endAt": "2026-10-14T15:00:00+09:00"}
								"""))
				.andExpect(status().isCreated());
		verify(createReservationUseCase).execute(eq(7L), any(), eq("key-1"));
	}

	@Test
	void logoutPassesCurrentToken() throws Exception {
		mockMvc.perform(post("/api/v1/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk());
		verify(logoutUseCase).execute(org.mockito.ArgumentMatchers.argThat(user ->
				user.userId().equals(7L) && user.tokenId() != null && user.tokenExpiresAt() != null));
	}

	@Test
	void cancelReturnsCommonSuccess() throws Exception {
		mockMvc.perform(delete("/api/v1/reservations/3").header(HttpHeaders.AUTHORIZATION, bearer(7L)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
		verify(cancelReservationUseCase).execute(7L, 3L);
	}

}
