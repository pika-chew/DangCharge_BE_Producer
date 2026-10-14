package com.picka_chew.dangcharge.api.reservation.port;

import java.util.Optional;

/**
 * Idempotency-Key로 같은 예약 요청이 두 번 처리되지 않게 한다. 예약과 같은 트랜잭션에서 저장하므로
 * 예약이 롤백되면 키도 남지 않아 같은 키로 다시 시도할 수 있다.
 * 구현체: infra/idempotency/JpaIdempotencyStore
 */
public interface IdempotencyStore {

	/** 이 키로 이미 만든 예약 ID */
	Optional<Long> findReservationId(Long userId, String key);

	void save(Long userId, String key, Long reservationId);

}
