package com.picka_chew.dangcharge.domain.reservation.validator;

import java.time.Instant;
import java.time.LocalDate;

import com.picka_chew.dangcharge.common.annotation.Validator;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.policy.ReservationTimePolicy;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import lombok.RequiredArgsConstructor;

@Validator
@RequiredArgsConstructor
public class ReservationValidatorImpl implements ReservationValidator {

	private final ReservationAdaptor reservationAdaptor;

	@Override
	public void validateStartTime(Instant startsAt, Instant now) {
		boolean valid = startsAt.isAfter(now)
				&& ReservationTimePolicy.isSlotAligned(startsAt)
				&& ReservationTimePolicy.isBookableDate(LocalDate.ofInstant(startsAt, ReservationTimePolicy.ZONE), now);
		if (!valid) {
			throw new GeneralException(ErrorStatus.INVALID_RESERVATION_TIME);
		}
	}

	@Override
	public void validateVehicleCompatible(Station station, VehicleModel vehicleModel) {
		if (!station.supports(vehicleModel)) {
			throw new GeneralException(ErrorStatus.VEHICLE_NOT_COMPATIBLE);
		}
	}

	@Override
	public void validateChargerUsable(Charger charger) {
		if (!charger.isUsable()) {
			throw new GeneralException(ErrorStatus.CHARGER_UNAVAILABLE);
		}
	}

	@Override
	public void validateNoOverlap(Long chargerId, Instant startsAt, Instant endsAt) {
		if (reservationAdaptor.existsOverlap(chargerId, startsAt, endsAt)) {
			throw new GeneralException(ErrorStatus.RESERVATION_CONFLICT);
		}
	}

	@Override
	public void validateNoActiveReservation(Long userId, Instant now) {
		if (reservationAdaptor.existsActiveByUserId(userId, now)) {
			throw new GeneralException(ErrorStatus.RESERVATION_ALREADY_ACTIVE);
		}
	}

	@Override
	public void validateCancelable(Reservation reservation, Instant now) {
		if (now.isAfter(reservation.getStartsAt().minus(ReservationTimePolicy.CANCEL_DEADLINE))) {
			throw new GeneralException(ErrorStatus.RESERVATION_NOT_CANCELABLE);
		}
	}

	@Override
	public void validateStoppable(Reservation reservation, Instant now) {
		if (now.isBefore(reservation.getStartsAt())) {
			throw new GeneralException(ErrorStatus.RESERVATION_NOT_STARTED);
		}
	}

}
