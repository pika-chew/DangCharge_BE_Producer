package com.picka_chew.dangcharge.api.auth.port;

import java.time.Duration;

/**
 * 로그아웃한 액세스 토큰(jti)을 남은 만료 시간 동안 막는다.
 * 구현체: infra/redis/RedisAccessTokenBlacklist
 */
public interface AccessTokenBlacklist {

	void add(String tokenId, Duration ttl);

	boolean contains(String tokenId);

}
