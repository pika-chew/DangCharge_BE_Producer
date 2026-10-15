package com.picka_chew.dangcharge.api.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.picka_chew.dangcharge.api.auth.port.AccessTokenBlacklist;
import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.api.auth.service.LoginUseCase;
import com.picka_chew.dangcharge.api.auth.service.LogoutUseCase;
import com.picka_chew.dangcharge.api.auth.service.RefreshTokenUseCase;
import com.picka_chew.dangcharge.api.auth.service.SignUpUseCase;
import com.picka_chew.dangcharge.api.charging.service.GetChargingHistoriesUseCase;
import com.picka_chew.dangcharge.api.charging.service.GetChargingHistoryUseCase;
import com.picka_chew.dangcharge.api.charging.service.StopChargingUseCase;
import com.picka_chew.dangcharge.api.reservation.service.CancelReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.GetMyReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.GetReservationUseCase;
import com.picka_chew.dangcharge.api.station.service.GetAvailabilityUseCase;
import com.picka_chew.dangcharge.api.station.service.GetChargersUseCase;
import com.picka_chew.dangcharge.api.station.service.GetNearbyStationsUseCase;
import com.picka_chew.dangcharge.api.station.service.GetStationUseCase;
import com.picka_chew.dangcharge.api.station.service.SearchStationsUseCase;
import com.picka_chew.dangcharge.api.station.service.SyncStationsUseCase;
import com.picka_chew.dangcharge.api.user.service.GetMyInfoUseCase;
import com.picka_chew.dangcharge.api.user.service.UpdateMyInfoUseCase;
import com.picka_chew.dangcharge.api.vehicle.service.GetVehicleModelsUseCase;
import com.picka_chew.dangcharge.common.config.TimeConfig;
import com.picka_chew.dangcharge.security.config.JwtConfig;
import com.picka_chew.dangcharge.security.config.SecurityConfig;
import com.picka_chew.dangcharge.security.service.JwtTokenIssuer;

/**
 * 컨트롤러 슬라이스 테스트 공통 설정. 실제 보안 설정과 JWT 발급기를 쓰고, UseCase와 토큰 저장소 포트는 목으로 바꾼다.
 */
@WebMvcTest
@Import({SecurityConfig.class, JwtConfig.class, JwtTokenIssuer.class, TimeConfig.class})
@TestPropertySource(properties = "dangcharge.admin.api-key=" + ControllerTestSupport.ADMIN_KEY)
public abstract class ControllerTestSupport {

	protected static final String ADMIN_KEY = "test-admin-key";

	@Autowired
	protected MockMvc mockMvc;
	@Autowired
	protected JwtTokenIssuer tokenIssuer;

	@MockitoBean
	protected AccessTokenBlacklist accessTokenBlacklist;
	@MockitoBean
	protected RefreshTokenStore refreshTokenStore;

	@MockitoBean
	protected SignUpUseCase signUpUseCase;
	@MockitoBean
	protected LoginUseCase loginUseCase;
	@MockitoBean
	protected RefreshTokenUseCase refreshTokenUseCase;
	@MockitoBean
	protected LogoutUseCase logoutUseCase;
	@MockitoBean
	protected GetMyInfoUseCase getMyInfoUseCase;
	@MockitoBean
	protected UpdateMyInfoUseCase updateMyInfoUseCase;
	@MockitoBean
	protected GetVehicleModelsUseCase getVehicleModelsUseCase;
	@MockitoBean
	protected GetNearbyStationsUseCase getNearbyStationsUseCase;
	@MockitoBean
	protected SearchStationsUseCase searchStationsUseCase;
	@MockitoBean
	protected GetStationUseCase getStationUseCase;
	@MockitoBean
	protected GetChargersUseCase getChargersUseCase;
	@MockitoBean
	protected GetAvailabilityUseCase getAvailabilityUseCase;
	@MockitoBean
	protected SyncStationsUseCase syncStationsUseCase;
	@MockitoBean
	protected CreateReservationUseCase createReservationUseCase;
	@MockitoBean
	protected GetMyReservationUseCase getMyReservationUseCase;
	@MockitoBean
	protected GetReservationUseCase getReservationUseCase;
	@MockitoBean
	protected CancelReservationUseCase cancelReservationUseCase;
	@MockitoBean
	protected StopChargingUseCase stopChargingUseCase;
	@MockitoBean
	protected GetChargingHistoriesUseCase getChargingHistoriesUseCase;
	@MockitoBean
	protected GetChargingHistoryUseCase getChargingHistoryUseCase;

	protected String bearer(Long userId) {
		return "Bearer " + tokenIssuer.issueAccessToken(userId);
	}

}
