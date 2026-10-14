package com.picka_chew.dangcharge.api.reservation.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;

public record ReservationResponse(Long reservationId, Long stationId, String stationName, Long chargerId,
		OffsetDateTime startAt, OffsetDateTime endAt, ReservationStatus status, BigDecimal estimatedFee) {
}
