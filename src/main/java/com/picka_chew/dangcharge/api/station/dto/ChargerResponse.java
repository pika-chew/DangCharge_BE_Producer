package com.picka_chew.dangcharge.api.station.dto;

import java.math.BigDecimal;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;

public record ChargerResponse(Long chargerId, ChargingType chargingType, String connectorType, BigDecimal maxPowerKw,
		OperationalStatus status) {
}
