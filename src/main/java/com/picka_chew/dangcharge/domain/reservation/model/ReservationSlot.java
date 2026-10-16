package com.picka_chew.dangcharge.domain.reservation.model;

import java.time.Instant;

/**
 * 충전기 하나의 예약 시간대(시작부터 1시간).
 *
 * @param reserved 진행 중인 예약(예약·충전중)과 겹치면 true. 이 시간대로는 예약할 수 없다
 */
public record ReservationSlot(Long chargerId, Instant startsAt, Instant endsAt, boolean reserved) {
}
