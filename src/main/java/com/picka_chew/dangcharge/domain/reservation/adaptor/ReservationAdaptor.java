package com.picka_chew.dangcharge.domain.reservation.adaptor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;

public interface ReservationAdaptor {

	/** 내 예약. 상태는 묻지 않는다. 없거나 남의 예약이면 RESERVATION_NOT_FOUND */
	Reservation getByIdAndUserId(Long reservationId, Long userId);

	/** 진행 중(CONFIRMED)인 내 예약. 없거나 남의 예약이면 RESERVATION_NOT_FOUND */
	Reservation getActiveByIdAndUserId(Long reservationId, Long userId);

	/** 충전을 마친(COMPLETED) 내 예약. 없으면 CHARGING_HISTORY_NOT_FOUND */
	Reservation getCompletedByIdAndUserId(Long reservationId, Long userId);

	boolean existsOverlap(Long chargerId, Instant startsAt, Instant endsAt);

	/** 아직 끝나지 않은 CONFIRMED 예약이 있는지 */
	boolean existsActiveByUserId(Long userId, Instant now);

	/** 아직 끝나지 않은 CONFIRMED 예약. 사용자당 하나다 */
	Optional<Reservation> findActiveByUserId(Long userId, Instant now);

	/** 주어진 충전기들의 CONFIRMED 예약 중 기간과 겹치는 것 */
	List<Reservation> findConfirmedOverlapping(Collection<Long> chargerIds, Instant from, Instant to);

	Page<Reservation> findCompletedByUserId(Long userId, Pageable pageable);

}
