package com.picka_chew.dangcharge.domain.reservation.adaptor;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.common.annotation.Adaptor;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class ReservationAdaptorImpl implements ReservationAdaptor {

	private final ReservationRepository reservationRepository;

	@Override
	public Reservation getByIdAndUserId(Long reservationId, Long userId) {
		return reservationRepository.findWithStationByIdAndUserId(reservationId, userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.RESERVATION_NOT_FOUND));
	}

	@Override
	public Reservation getActiveByIdAndUserId(Long reservationId, Long userId) {
		return reservationRepository.findWithStationByIdAndUserId(reservationId, userId)
				.filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
				.orElseThrow(() -> new GeneralException(ErrorStatus.RESERVATION_NOT_FOUND));
	}

	@Override
	public Reservation getCompletedByIdAndUserId(Long reservationId, Long userId) {
		return reservationRepository.findWithStationByIdAndUserId(reservationId, userId)
				.filter(reservation -> reservation.getStatus() == ReservationStatus.COMPLETED)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CHARGING_HISTORY_NOT_FOUND));
	}

	@Override
	public boolean existsOverlap(Long chargerId, Instant startsAt, Instant endsAt) {
		return reservationRepository.existsByChargerIdAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
				chargerId, ReservationStatus.CONFIRMED, endsAt, startsAt);
	}

	@Override
	public boolean existsActiveByUserId(Long userId, Instant now) {
		return reservationRepository.existsByUserIdAndStatusAndEndsAtGreaterThan(userId,
				ReservationStatus.CONFIRMED, now);
	}

	@Override
	public Optional<Reservation> findActiveByUserId(Long userId, Instant now) {
		return reservationRepository.findFirstByUserIdAndStatusAndEndsAtGreaterThanOrderByStartsAtDesc(userId,
				ReservationStatus.CONFIRMED, now);
	}

	@Override
	public List<Reservation> findConfirmedOverlapping(Collection<Long> chargerIds, Instant from, Instant to) {
		if (chargerIds.isEmpty()) {
			return List.of();
		}
		return reservationRepository.findAllByChargerIdInAndStatusAndStartsAtLessThanAndEndsAtGreaterThan(
				chargerIds, ReservationStatus.CONFIRMED, to, from);
	}

	@Override
	public Page<Reservation> findCompletedByUserId(Long userId, Pageable pageable) {
		return reservationRepository.findWithStationByUserIdAndStatus(userId, ReservationStatus.COMPLETED, pageable);
	}

}
