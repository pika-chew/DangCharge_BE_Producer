package com.picka_chew.dangcharge.api.auth.port;

import java.time.Duration;
import java.util.Optional;

/**
 * 사용자별로 유효한 리프레시 토큰 하나를 보관한다.
 * 구현체: infra/redis/RedisRefreshTokenStore
 */
public interface RefreshTokenStore {

	void save(Long userId, String refreshToken, Duration ttl);

	Optional<String> find(Long userId);

	void delete(Long userId);

}
