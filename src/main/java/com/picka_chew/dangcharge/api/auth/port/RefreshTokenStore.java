package com.picka_chew.dangcharge.api.auth.port;

import java.time.Duration;

/**
 * 사용자별로 유효한 리프레시 토큰 하나를 보관한다. 토큰 원문이 아니라 해시만 저장한다.
 * 구현체: infra/token/JpaRefreshTokenStore
 */
public interface RefreshTokenStore {

	/** 이전에 저장된 토큰이 있으면 덮어쓴다 */
	void save(Long userId, String refreshToken, Duration ttl);

	/** 저장된 토큰과 같고 아직 만료되지 않았는지 */
	boolean matches(Long userId, String refreshToken);

	void delete(Long userId);

}
