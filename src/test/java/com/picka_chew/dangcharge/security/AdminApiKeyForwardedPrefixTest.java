package com.picka_chew.dangcharge.security;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.picka_chew.dangcharge.api.common.ControllerTestSupport;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;

/**
 * 서버처럼 Traefik이 /command를 떼고 X-Forwarded-Prefix를 붙이고, 앱이 그 헤더를 읽는(forward-headers-strategy=framework)
 * 상황에서도 관리 API 키가 동작하는지 확인한다. 헤더를 읽으면 요청 URI가 /command/api/v1/admin/...으로 바뀐다.
 */
@Import(AdminApiKeyForwardedPrefixTest.ForwardedHeaderConfig.class)
class AdminApiKeyForwardedPrefixTest extends ControllerTestSupport {

	@TestConfiguration
	static class ForwardedHeaderConfig {

		/** forward-headers-strategy=framework와 같이 보안 필터보다 먼저 헤더를 읽는다 */
		@Bean
		FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
			FilterRegistrationBean<ForwardedHeaderFilter> registration =
					new FilterRegistrationBean<>(new ForwardedHeaderFilter());
			registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return registration;
		}

	}

	@Test
	void adminKeyWorksWhenIngressStripsPrefix() throws Exception {
		given(syncStationsUseCase.execute()).willReturn(new StationSyncResult(0, 0, List.of()));

		mockMvc.perform(post("/api/v1/admin/stations/sync")
						.header("X-Forwarded-Prefix", "/command")
						.header("X-Admin-Key", ADMIN_KEY))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/admin/stations/sync")
						.header("X-Forwarded-Prefix", "/command"))
				.andExpect(status().isUnauthorized());
	}

}
