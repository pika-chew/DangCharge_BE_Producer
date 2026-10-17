package com.picka_chew.dangcharge.domain.reservation.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	/** 시간이 겹치는 진행 중 예약. 조건은 기존.시작 < 새.종료 AND 기존.종료 > 새.시작 */
	boolean existsByChargerIdAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(Long chargerId,
			Collection<ReservationStatus> statuses, Instant endsAt, Instant startsAt);

	/** 진행 중인 예약: 아직 끝나지 않은 CONFIRMED, 또는 종료 시각과 관계없이 CHARGING */
	@Query("""
			select count(r) > 0 from Reservation r
			where r.user.id = :userId
			and (r.status = com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus.CHARGING
				or (r.status = com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus.CONFIRMED
					and r.endsAt > :now))
			""")
	boolean existsInProgressByUserId(@Param("userId") Long userId, @Param("now") Instant now);

	@Query("""
			select r from Reservation r
			where r.user.id = :userId
			and (r.status = com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus.CHARGING
				or (r.status = com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus.CONFIRMED
					and r.endsAt > :now))
			order by r.startsAt desc
			""")
	List<Reservation> findInProgressByUserId(@Param("userId") Long userId, @Param("now") Instant now);

	/**
	 * 노쇼 처리 대상(충전 시작 마감이 지난 CONFIRMED 예약)을 잠그며 가져온다.
	 * 다른 파드가 잠근 행은 건너뛰어 레플리카가 여럿이어도 같은 예약을 두 번 처리하지 않는다.
	 */
	@NativeQuery("""
			select * from reservations
			where status = 'CONFIRMED' and starts_at < :startedBefore
			order by starts_at, id
			limit :limit
			for update skip locked
			""")
	List<Reservation> lockNoShowTargets(@Param("startedBefore") Instant startedBefore, @Param("limit") int limit);

	@Query("""
			select r from Reservation r join fetch r.charger c join fetch c.station
			where r.id = :id and r.user.id = :userId
			""")
	Optional<Reservation> findWithStationByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	List<Reservation> findAllByChargerIdInAndStatusInAndStartsAtLessThanAndEndsAtGreaterThan(
			Collection<Long> chargerIds, Collection<ReservationStatus> statuses, Instant endsAt, Instant startsAt);

	@Query(value = """
			select r from Reservation r join fetch r.charger c join fetch c.station
			where r.user.id = :userId and r.status = :status
			order by r.completedAt desc, r.id desc
			""",
			countQuery = "select count(r) from Reservation r where r.user.id = :userId and r.status = :status")
	Page<Reservation> findWithStationByUserIdAndStatus(@Param("userId") Long userId,
			@Param("status") ReservationStatus status, Pageable pageable);

}
