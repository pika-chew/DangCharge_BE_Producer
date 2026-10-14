package com.picka_chew.dangcharge.api.station.dto;

import java.time.OffsetDateTime;

public record AvailableSlotResponse(Long chargerId, OffsetDateTime startAt, OffsetDateTime endAt) {
}
