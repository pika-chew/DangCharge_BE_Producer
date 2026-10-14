package com.picka_chew.dangcharge.api.charging.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/**
 * 충전 내역 한 건.
 *
 * @param stationName 충전소 이름
 * @param stationAddress 충전소 주소
 * @param vehicleModel 충전한 차종
 * @param reservedStartAt 예약 시작 시각
 * @param reservedEndAt 예약 종료 시각
 * @param completedAt 충전 완료 시각
 * @param sessionId 충전 세션 테이블이 없어 예약 ID를 쓴다
 */
public record ChargingHistoryResponse(Long sessionId, Long stationId, String stationName, String stationAddress,
		VehicleModel vehicleModel, OffsetDateTime reservedStartAt, OffsetDateTime reservedEndAt,
		OffsetDateTime completedAt) {

	public static ChargingHistoryResponse from(Reservation reservation) {
		Station station = reservation.getCharger().getStation();
		return new ChargingHistoryResponse(reservation.getId(), station.getId(), station.getName(),
				station.getAddress(), reservation.getVehicleModel(),
				TimeUtils.toOffsetDateTime(reservation.getStartsAt()), TimeUtils.toOffsetDateTime(reservation.getEndsAt()),
				TimeUtils.toOffsetDateTime(reservation.getCompletedAt()));
	}

}
