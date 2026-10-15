package com.picka_chew.dangcharge.security.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 관리 API(/api/v1/admin/**)는 사용자 로그인 대신 X-Admin-Key 헤더로 인증한다.
 * 키가 설정되지 않았으면 아무도 통과하지 못한다.
 */
public class AdminApiKeyFilter extends OncePerRequestFilter {

	public static final String HEADER = "X-Admin-Key";
	public static final String ADMIN_PATH_PREFIX = "/api/v1/admin/";

	private final byte[] adminKey;

	public AdminApiKeyFilter(String adminKey) {
		this.adminKey = adminKey == null ? new byte[0] : adminKey.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// 요청 URI에는 context-path(/command 등)가 붙어 있어 떼고 비교한다
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return !path.startsWith(ADMIN_PATH_PREFIX);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String provided = request.getHeader(HEADER);
		if (adminKey.length > 0 && provided != null
				&& MessageDigest.isEqual(adminKey, provided.getBytes(StandardCharsets.UTF_8))) {
			SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
					"admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
		}
		chain.doFilter(request, response);
	}

}
