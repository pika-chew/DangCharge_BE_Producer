package com.picka_chew.dangcharge.infra.idempotency;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.reservation.port.IdempotencyStore;

import lombok.RequiredArgsConstructor;

/** 예약 생성 트랜잭션 안에서만 쓴다 */
@Component
@RequiredArgsConstructor
public class JpaIdempotencyStore implements IdempotencyStore {

	private final ReservationIdempotencyKeyRepository repository;
	private final Clock clock;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Optional<Long> findReservationId(Long userId, String key) {
		return repository.findByUserIdAndIdempotencyKey(userId, key).map(ReservationIdempotencyKey::getReservationId);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void save(Long userId, String key, Long reservationId) {
		repository.save(new ReservationIdempotencyKey(userId, key, reservationId, Instant.now(clock)));
	}

}
