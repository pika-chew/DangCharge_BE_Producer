package com.picka_chew.dangcharge.api.auth.port;

import java.time.Duration;

/**
 * 액세스·리프레시 토큰을 발급하고 리프레시 토큰을 검증한다.
 * 구현체: security/service/JwtTokenIssuer
 */
public interface TokenIssuer {

	String issueAccessToken(Long userId);

	String issueRefreshToken(Long userId);

	/** 서명, 만료, 토큰 종류를 검증하고 사용자 ID를 돌려준다. 유효하지 않으면 INVALID_REFRESH_TOKEN */
	Long verifyRefreshToken(String refreshToken);

	Duration refreshTokenTtl();

}
