package com.picka_chew.dangcharge.api.charging.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ChargingHistoryResponse(Long sessionId, String stationName, OffsetDateTime startedAt, OffsetDateTime endedAt,
		BigDecimal chargedKwh, BigDecimal fee) {
}
