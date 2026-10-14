package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/** 예약 차종은 로그인 사용자의 차종을 쓴다 */
public record CreateReservationRequest(
		@NotNull Long chargerId,
		@NotNull OffsetDateTime startAt,
		@NotNull OffsetDateTime endAt) {
}
