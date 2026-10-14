package com.picka_chew.dangcharge.api.charging.dto;

import java.math.BigDecimal;

public record ChargingSessionResponse(Long sessionId, BigDecimal chargedPercent, Long remainingMinutes, BigDecimal currentFee) {
}
