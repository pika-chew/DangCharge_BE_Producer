package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * @param vehicleId 스펙에 있지만 차량 엔티티가 없어 쓰지 않는다. 예약 차종은 로그인 사용자의 차종이다
 */
public record CreateReservationRequest(
		@NotNull Long chargerId,
		Long vehicleId,
		@NotNull OffsetDateTime startAt,
		@NotNull OffsetDateTime endAt) {
}
