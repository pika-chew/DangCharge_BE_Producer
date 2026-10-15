package com.picka_chew.dangcharge.api.user.dto;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 바꿀 값만 보낸다. null인 값은 그대로 둔다 */
public record UpdateUserRequest(
		@Size(min = 1, max = 50) @Pattern(regexp = ".*\\S.*", message = "공백만으로는 만들 수 없습니다") String username,
		VehicleModel vehicleModel) {
}
