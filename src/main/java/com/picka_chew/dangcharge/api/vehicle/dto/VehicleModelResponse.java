package com.picka_chew.dangcharge.api.vehicle.dto;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record VehicleModelResponse(VehicleModel code, String label) {
}
