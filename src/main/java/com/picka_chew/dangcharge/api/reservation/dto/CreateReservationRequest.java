package com.picka_chew.dangcharge.api.reservation.dto;

import java.time.OffsetDateTime;

public record CreateReservationRequest(Long chargerId, Long vehicleId, OffsetDateTime startAt, OffsetDateTime endAt) {
}
