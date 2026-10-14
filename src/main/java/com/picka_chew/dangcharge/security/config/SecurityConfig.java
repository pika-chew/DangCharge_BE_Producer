package com.picka_chew.dangcharge.security.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.security.filter.AdminApiKeyFilter;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String[] PUBLIC_DOCS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
			"/actuator/health/**", "/error"};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JsonMapper jsonMapper,
			@Value("${dangcharge.admin.api-key:}") String adminApiKey) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_DOCS).permitAll()
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/signup", "/api/v1/auth/login",
								"/api/v1/auth/refresh").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/v1/vehicle-models", "/api/v1/stations",
								"/api/v1/stations/search", "/api/v1/stations/{stationId}",
								"/api/v1/stations/{stationId}/chargers").permitAll()
						.requestMatchers(AdminApiKeyFilter.ADMIN_PATH_PREFIX + "**").hasRole("ADMIN")
						.anyRequest().authenticated())
				.addFilterBefore(new AdminApiKeyFilter(adminApiKey), BearerTokenAuthenticationFilter.class)
				.oauth2ResourceServer(resourceServer -> resourceServer
						.jwt(jwt -> {
						})
						.authenticationEntryPoint((request, response, exception) ->
								writeError(response, jsonMapper, ErrorStatus.UNAUTHORIZED))
						.accessDeniedHandler((request, response, exception) ->
								writeError(response, jsonMapper, ErrorStatus.FORBIDDEN)))
				.exceptionHandling(handling -> handling
						.authenticationEntryPoint((request, response, exception) ->
								writeError(response, jsonMapper, ErrorStatus.UNAUTHORIZED))
						.accessDeniedHandler((request, response, exception) ->
								writeError(response, jsonMapper, ErrorStatus.FORBIDDEN)))
				.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	private static void writeError(HttpServletResponse response, JsonMapper jsonMapper, ErrorStatus errorStatus)
			throws IOException {
		response.setStatus(errorStatus.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jsonMapper.writeValueAsString(ApiResponseDto.onFailure(errorStatus)));
	}

}
