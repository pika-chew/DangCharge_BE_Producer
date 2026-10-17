package com.picka_chew.dangcharge.api.common;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** k8s probe가 로그인 없이 헬스 체크 경로를 부를 수 있고, 다른 actuator 엔드포인트는 열려 있지 않은지 확인한다 */
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false",
		"dangcharge.no-show.enabled=false"})
@AutoConfigureMockMvc
class HealthCheckTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void healthAndProbesAreOpenWithoutLogin() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void healthDoesNotExposeComponentDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(jsonPath("$", not(hasKey("components"))));
	}

	@Test
	void otherActuatorEndpointsAreNotExposed() throws Exception {
		mockMvc.perform(get("/actuator/env")).andExpect(status().is4xxClientError());
		mockMvc.perform(get("/actuator/beans")).andExpect(status().is4xxClientError());
	}

}
