package com.picka_chew.dangcharge.api.station.dto;

import java.math.BigDecimal;

public record StationMarkerResponse(Long stationId, String name, BigDecimal latitude, BigDecimal longitude, int availableChargerCount) {
}
