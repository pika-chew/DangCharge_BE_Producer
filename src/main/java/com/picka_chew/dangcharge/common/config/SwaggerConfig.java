package com.picka_chew.dangcharge.common.config;

import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.picka_chew.dangcharge.common.annotation.CurrentUser;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

	public static final String BEARER_SCHEME = "bearerAuth";
	public static final String ADMIN_KEY_SCHEME = "adminKey";

	static {
		// 로그인 사용자 파라미터는 토큰에서 채우므로 문서에 노출하지 않는다
		SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
	}

	/** 기본은 Bearer 토큰이 필요하다. 로그인 없이 쓰는 API는 @SecurityRequirements로 비운다 */
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("DangCharge API")
						.description("DangCharge 서비스 API 문서")
						.version("v1"))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT"))
						.addSecuritySchemes(ADMIN_KEY_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.name("X-Admin-Key")));
	}

}
