package com.picka_chew.dangcharge.infra.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.auth.port.AccessTokenBlacklist;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisAccessTokenBlacklist implements AccessTokenBlacklist {

	private static final String PREFIX = "blacklist:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void add(String tokenId, Duration ttl) {
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}
		redisTemplate.opsForValue().set(PREFIX + tokenId, "1", ttl);
	}

	@Override
	public boolean contains(String tokenId) {
		return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + tokenId));
	}

}
