package com.picka_chew.dangcharge.domain.reservation.validator;

import java.time.Instant;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public interface ReservationValidator {

	/** 미래 시각, 정각 또는 30분, 오늘부터 7일 이내. 아니면 INVALID_RESERVATION_TIME */
	void validateStartTime(Instant startsAt, Instant now);

	/** 충전소가 차종을 지원하지 않으면 VEHICLE_NOT_COMPATIBLE */
	void validateVehicleCompatible(Station station, VehicleModel vehicleModel);

	/** 고장·점검 중이면 CHARGER_UNAVAILABLE */
	void validateChargerUsable(Charger charger);

	/** 같은 충전기의 CONFIRMED 예약과 겹치면 RESERVATION_CONFLICT */
	void validateNoOverlap(Long chargerId, Instant startsAt, Instant endsAt);

	/** 사용자는 진행 중인 예약을 하나만 가질 수 있다. 이미 있으면 RESERVATION_ALREADY_ACTIVE */
	void validateNoActiveReservation(Long userId, Instant now);

	/** 예약(CONFIRMED) 상태가 아니면 INVALID_RESERVATION_STATUS, 시작 10분 전이 지났으면 RESERVATION_NOT_CANCELABLE */
	void validateCancelable(Reservation reservation, Instant now);

	/** 예약 시작 10분 전 ~ 시작 15분 후가 아니면 CHECK_IN_NOT_ALLOWED */
	void validateCheckInWindow(Reservation reservation, Instant now);

}
