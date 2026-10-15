package com.picka_chew.dangcharge.api.common;

import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Import;

import com.picka_chew.dangcharge.common.config.SwaggerConfig;

/** 컨트롤러가 OpenAPI 문서에 실리는지 확인한다 */
@Import(SwaggerConfig.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class, org.springdoc.webmvc.ui.SwaggerConfig.class,
		SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class})
class ApiDocsTest extends ControllerTestSupport {

	@Test
	void apiDocsListControllers() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("DangCharge API"))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/stations/{stationId}")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/charging/{reservationId}/stop")))
				.andExpect(jsonPath("$.paths['/api/v1/vehicle-models'].get.summary").value("차종 목록"))
				.andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
	}

	@Test
	void swaggerUiPathRedirectsToUi() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
	}

	/** 인그레스가 /command를 떼지 않고 넘기면 앱은 context-path /command 아래에서 동작한다 */
	@Test
	void swaggerKeepsContextPathInRedirectAndApiDocsUrl() throws Exception {
		mockMvc.perform(get("/command/swagger-ui.html").contextPath("/command"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/command/swagger-ui/index.html"));
		mockMvc.perform(get("/command/v3/api-docs/swagger-config").contextPath("/command"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value("/command/v3/api-docs"));
	}

}
