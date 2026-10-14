package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotNull;

/**
 * 예약 차종은 로그인 사용자의 차종을 쓰고, 종료 시각은 시작 시각에서 1시간 뒤로 서버가 정한다.
 */
public record CreateReservationRequest(
		@NotNull Long chargerId,
		@NotNull OffsetDateTime startAt) {
}
