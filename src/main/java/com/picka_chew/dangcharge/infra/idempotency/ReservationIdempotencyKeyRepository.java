package com.picka_chew.dangcharge.infra.idempotency;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationIdempotencyKeyRepository extends JpaRepository<ReservationIdempotencyKey, Long> {

	Optional<ReservationIdempotencyKey> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

	@Modifying
	@Query("delete from ReservationIdempotencyKey k where k.createdAt < :before")
	int deleteCreatedBefore(@Param("before") Instant before);

}
