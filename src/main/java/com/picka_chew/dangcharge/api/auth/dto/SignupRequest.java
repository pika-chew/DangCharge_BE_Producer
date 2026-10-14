package com.picka_chew.dangcharge.api.auth.dto;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record SignupRequest(String loginId, String username, String password, VehicleModel vehicleModel) {
}
