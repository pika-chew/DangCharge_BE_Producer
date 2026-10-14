package com.picka_chew.dangcharge.infra.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private static final String PREFIX = "refresh:";

	private final StringRedisTemplate redisTemplate;

	@Override
	public void save(Long userId, String refreshToken, Duration ttl) {
		redisTemplate.opsForValue().set(PREFIX + userId, refreshToken, ttl);
	}

	@Override
	public Optional<String> find(Long userId) {
		return Optional.ofNullable(redisTemplate.opsForValue().get(PREFIX + userId));
	}

	@Override
	public void delete(Long userId) {
		redisTemplate.delete(PREFIX + userId);
	}

}
