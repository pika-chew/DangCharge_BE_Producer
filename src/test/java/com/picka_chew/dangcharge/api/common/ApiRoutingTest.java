package com.picka_chew.dangcharge.api.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** API 스펙의 모든 엔드포인트가 매핑되어 있고, 구현 전이라 공통 포맷의 501을 돌려주는지 확인한다 */
@WebMvcTest
class ApiRoutingTest {

	@Autowired
	private MockMvc mockMvc;

	@ParameterizedTest
	@CsvSource({
			"POST,   /api/v1/auth/signup",
			"POST,   /api/v1/auth/login",
			"POST,   /api/v1/auth/refresh",
			"POST,   /api/v1/auth/logout",
			"GET,    /api/v1/users/me",
			"PATCH,  /api/v1/users/me",
			"GET,    /api/v1/vehicle-models",
			"GET,    /api/v1/stations?lat=37.5&lng=127.0&radius=1000",
			"GET,    /api/v1/stations?lat=37.5&lng=127.0&radius=1000&connectorType=DC_COMBO&minKw=50&availableOnly=true",
			"GET,    /api/v1/stations/search?keyword=강남&metro=서울특별시&city=강남구",
			"GET,    /api/v1/stations/1",
			"GET,    /api/v1/stations/1/chargers",
			"GET,    /api/v1/stations/1/availability?date=2026-10-14&vehicleId=5",
			"POST,   /api/v1/reservations",
			"GET,    /api/v1/reservations/1",
			"DELETE, /api/v1/reservations/1",
			"POST,   /api/v1/charging/1/stop",
			"GET,    /api/v1/charging/history",
			"GET,    /api/v1/charging/history?page=1&size=10",
			"GET,    /api/v1/charging/history/1",
			"POST,   /api/v1/admin/stations/sync"
	})
	void everySpecEndpointIsMapped(String method, String uri) throws Exception {
		mockMvc.perform(request(HttpMethod.valueOf(method), uri)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isNotImplemented())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("NOT_IMPLEMENTED"));
	}

	@Test
	void reservationRequestBodyFromSpecIsReadable() throws Exception {
		mockMvc.perform(request(HttpMethod.POST, "/api/v1/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"chargerId": 1203, "vehicleId": 5,
								"startAt": "2026-10-14T14:00:00+09:00", "endAt": "2026-10-14T15:00:00+09:00"}
								"""))
				.andExpect(status().isNotImplemented());
	}

}
