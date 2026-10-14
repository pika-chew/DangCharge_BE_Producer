package com.picka_chew.dangcharge.api.user.dto;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record UpdateUserRequest(String username, VehicleModel vehicleModel) {
}
