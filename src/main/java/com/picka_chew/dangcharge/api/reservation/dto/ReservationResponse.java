package com.picka_chew.dangcharge.api.reservation.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

public record ReservationResponse(Long reservationId, Long stationId, String stationName, Long chargerId,
		OffsetDateTime startAt, OffsetDateTime endAt, ReservationStatus status, BigDecimal estimatedFee) {

	public static ReservationResponse from(Reservation reservation) {
		Station station = reservation.getCharger().getStation();
		return new ReservationResponse(reservation.getId(), station.getId(), station.getName(),
				reservation.getCharger().getId(), TimeUtils.toOffsetDateTime(reservation.getStartsAt()),
				TimeUtils.toOffsetDateTime(reservation.getEndsAt()), reservation.getStatus(),
				reservation.getEstimatedFee());
	}

}
