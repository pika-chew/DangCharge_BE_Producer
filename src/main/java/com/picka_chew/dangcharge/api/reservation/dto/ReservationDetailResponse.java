package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

/**
 * 예약 상세확인하기 화면. 예약 생성 응답도 이 형식이라 consumer 반영 전에도 바로 그릴 수 있다.
 *
 * @param reservationNumber 사용자에게 보여주는 예약 번호. 예: DC20261015-000001
 */
public record ReservationDetailResponse(Long reservationId, String reservationNumber, OffsetDateTime startAt,
		OffsetDateTime endAt, Long stationId, String stationName, String stationAddress, Long chargerId,
		ReservationStatus status) {

	public static ReservationDetailResponse from(Reservation reservation) {
		Station station = reservation.getCharger().getStation();
		return new ReservationDetailResponse(reservation.getId(), reservation.getReservationNumber(),
				TimeUtils.toOffsetDateTime(reservation.getStartsAt()), TimeUtils.toOffsetDateTime(reservation.getEndsAt()),
				station.getId(), station.getName(), station.getAddress(), reservation.getCharger().getId(),
				reservation.getStatus());
	}

}
