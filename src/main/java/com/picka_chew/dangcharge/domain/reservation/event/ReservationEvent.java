package com.picka_chew.dangcharge.domain.reservation.event;

import java.time.Instant;

import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/** 예약 생성·취소·노쇼·충전 시작·충전 완료 이벤트. consumer가 조인 없이 화면을 그릴 수 있게 충전소 정보까지 담는다 */
public record ReservationEvent(String eventType, Long reservationId, Long userId, Long chargerId, Long stationId,
		String stationName, String stationAddress, VehicleModel vehicleModel, Instant startsAt, Instant endsAt,
		ReservationStatus status, Instant chargingStartedAt, Instant completedAt) implements DomainEvent {

	public static ReservationEvent created(Reservation reservation) {
		return of("ReservationCreated", reservation);
	}

	public static ReservationEvent canceled(Reservation reservation) {
		return of("ReservationCanceled", reservation);
	}

	public static ReservationEvent noShow(Reservation reservation) {
		return of("ReservationNoShow", reservation);
	}

	public static ReservationEvent chargingStarted(Reservation reservation) {
		return of("ChargingStarted", reservation);
	}

	public static ReservationEvent completed(Reservation reservation) {
		return of("ChargingCompleted", reservation);
	}

	private static ReservationEvent of(String eventType, Reservation reservation) {
		Station station = reservation.getCharger().getStation();
		return new ReservationEvent(eventType, reservation.getId(), reservation.getUser().getId(),
				reservation.getCharger().getId(), station.getId(), station.getName(), station.getAddress(),
				reservation.getVehicleModel(), reservation.getStartsAt(), reservation.getEndsAt(),
				reservation.getStatus(), reservation.getChargingStartedAt(), reservation.getCompletedAt());
	}

	@Override
	public String aggregateType() {
		return "reservation";
	}

	@Override
	public Long aggregateId() {
		return reservationId;
	}

}
