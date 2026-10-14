package com.picka_chew.dangcharge.api.vehicle.dto;

import java.math.BigDecimal;

public record VehicleModelResponse(String manufacturer, String model, String connectorType, BigDecimal batteryCapacityKwh) {
}
