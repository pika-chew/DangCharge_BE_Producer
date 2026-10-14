package com.picka_chew.dangcharge.api.station.dto;

import java.math.BigDecimal;
import java.util.Set;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record StationDetailResponse(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels, BigDecimal latitude, BigDecimal longitude) {
}
