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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.picka_chew.dangcharge.common.config.SwaggerConfig;

/** 컨트롤러가 OpenAPI 문서에 실리는지 확인한다 */
@WebMvcTest
@Import(SwaggerConfig.class)
@ImportAutoConfiguration({SpringDocConfiguration.class, SpringDocConfigProperties.class,
		SpringDocWebMvcConfiguration.class, org.springdoc.webmvc.ui.SwaggerConfig.class,
		SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class})
class ApiDocsTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiDocsListControllers() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("DangCharge API"))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/stations/{stationId}")))
				.andExpect(jsonPath("$.paths", hasKey("/api/v1/charging/{sessionId}/stop")))
				.andExpect(jsonPath("$.paths['/api/v1/vehicle-models'].get.summary").value("차종 목록"));
	}

	@Test
	void swaggerUiPathRedirectsToUi() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/swagger-ui/index.html"));
	}

}
