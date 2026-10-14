package com.picka_chew.dangcharge.infra.redis;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.reservation.port.IdempotencyStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

	private static final String PREFIX = "idempotency:reservation:";
	private static final String PENDING = "PENDING";
	private static final Duration TTL = Duration.ofHours(24);

	private final StringRedisTemplate redisTemplate;

	@Override
	public boolean tryAcquire(Long userId, String key) {
		return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey(userId, key), PENDING, TTL));
	}

	@Override
	public Optional<Long> findCompletedReservationId(Long userId, String key) {
		String value = redisTemplate.opsForValue().get(redisKey(userId, key));
		if (value == null || PENDING.equals(value)) {
			return Optional.empty();
		}
		return Optional.of(Long.valueOf(value));
	}

	@Override
	public void complete(Long userId, String key, Long reservationId) {
		redisTemplate.opsForValue().set(redisKey(userId, key), String.valueOf(reservationId), TTL);
	}

	@Override
	public void release(Long userId, String key) {
		redisTemplate.delete(redisKey(userId, key));
	}

	private static String redisKey(Long userId, String key) {
		return PREFIX + userId + ":" + key;
	}

}
