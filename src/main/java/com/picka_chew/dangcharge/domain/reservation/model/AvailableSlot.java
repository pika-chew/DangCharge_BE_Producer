package com.picka_chew.dangcharge.domain.reservation.model;

import java.time.Instant;

/** 충전기 하나의 비어 있는 예약 슬롯 */
public record AvailableSlot(Long chargerId, Instant startsAt, Instant endsAt) {
}
