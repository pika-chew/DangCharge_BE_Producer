package com.picka_chew.dangcharge.domain.reservation.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	/** 시간이 겹치는 예약. 조건은 기존.시작 < 새.종료 AND 기존.종료 > 새.시작 */
	boolean existsByChargerIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(Long chargerId,
			ReservationStatus status, Instant endsAt, Instant startsAt);

	boolean existsByUserIdAndStatusAndEndsAtGreaterThan(Long userId, ReservationStatus status, Instant now);

	@Query("""
			select r from Reservation r join fetch r.charger c join fetch c.station
			where r.id = :id and r.user.id = :userId
			""")
	Optional<Reservation> findWithStationByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	List<Reservation> findAllByChargerIdInAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
			Collection<Long> chargerIds, ReservationStatus status, Instant endsAt, Instant startsAt);

	@Query(value = """
			select r from Reservation r join fetch r.charger c join fetch c.station
			where r.user.id = :userId and r.status = :status
			order by r.completedAt desc, r.id desc
			""",
			countQuery = "select count(r) from Reservation r where r.user.id = :userId and r.status = :status")
	Page<Reservation> findWithStationByUserIdAndStatus(@Param("userId") Long userId,
			@Param("status") ReservationStatus status, Pageable pageable);

}
