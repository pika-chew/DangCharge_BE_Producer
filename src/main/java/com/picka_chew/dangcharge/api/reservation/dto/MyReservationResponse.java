package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

/**
 * 내 예약 확인하기 화면. status는 CONFIRMED(예약) 또는 CHARGING(충전중)이다.
 * 진행 중인 예약이 없으면 hasReservation만 false이고 나머지는 null이다.
 */
public record MyReservationResponse(boolean hasReservation, Long reservationId, OffsetDateTime startAt,
		OffsetDateTime endAt, Long stationId, String stationName, String stationAddress, ReservationStatus status) {

	public static MyReservationResponse none() {
		return new MyReservationResponse(false, null, null, null, null, null, null, null);
	}

	public static MyReservationResponse from(Reservation reservation) {
		Station station = reservation.getCharger().getStation();
		return new MyReservationResponse(true, reservation.getId(),
				TimeUtils.toOffsetDateTime(reservation.getStartsAt()), TimeUtils.toOffsetDateTime(reservation.getEndsAt()),
				station.getId(), station.getName(), station.getAddress(), reservation.getStatus());
	}

}
