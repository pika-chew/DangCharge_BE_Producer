package com.picka_chew.dangcharge.api.user.dto;

import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record UserResponse(Long userId, String loginId, String username, VehicleModel vehicleModel) {

	public static UserResponse from(User user) {
		return new UserResponse(user.getId(), user.getLoginId(), user.getUsername(), user.getVehicleModel());
	}

}
