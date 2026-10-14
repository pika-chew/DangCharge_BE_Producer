package com.picka_chew.dangcharge.api.reservation.port;

import java.util.Optional;

/**
 * Idempotency-Key로 같은 예약 요청이 두 번 처리되지 않게 한다.
 * 구현체: infra/redis/RedisIdempotencyStore
 */
public interface IdempotencyStore {

	/** 키를 선점한다. 이미 누가 선점했으면 false */
	boolean tryAcquire(Long userId, String key);

	/** 처리가 끝난 키면 만들어진 예약 ID, 처리 중이면 empty */
	Optional<Long> findCompletedReservationId(Long userId, String key);

	void complete(Long userId, String key, Long reservationId);

	void release(Long userId, String key);

}
