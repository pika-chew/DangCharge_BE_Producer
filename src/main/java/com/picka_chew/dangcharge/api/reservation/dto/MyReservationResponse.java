package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

/**
 * 내 예약 확인하기 화면. 프론트는 state로 화면을 나누고 message를 그대로 띄울 수 있다.
 * 진행 중인 예약이 없으면 state가 NONE이고 reservation은 null이다.
 */
public record MyReservationResponse(State state, String message, Summary reservation) {

	public static MyReservationResponse none() {
		return new MyReservationResponse(State.NONE, State.NONE.message, null);
	}

	public static MyReservationResponse from(Reservation reservation) {
		State state = reservation.getStatus() == ReservationStatus.CHARGING ? State.CHARGING : State.RESERVED;
		return new MyReservationResponse(state, state.message, Summary.from(reservation));
	}

	public enum State {

		/** 진행 중인 예약 없음 */
		NONE("진행 중인 예약이 없습니다"),
		/** 예약됨, 아직 충전 시작 전 */
		RESERVED("예약되어 있습니다"),
		/** 충전 중 */
		CHARGING("충전 중입니다");

		private final String message;

		State(String message) {
			this.message = message;
		}

	}

	public record Summary(Long reservationId, OffsetDateTime startAt, OffsetDateTime endAt, Long stationId,
			String stationName, String stationAddress) {

		static Summary from(Reservation reservation) {
			Station station = reservation.getCharger().getStation();
			return new Summary(reservation.getId(), TimeUtils.toOffsetDateTime(reservation.getStartsAt()),
					TimeUtils.toOffsetDateTime(reservation.getEndsAt()), station.getId(), station.getName(),
					station.getAddress());
		}

	}

}
