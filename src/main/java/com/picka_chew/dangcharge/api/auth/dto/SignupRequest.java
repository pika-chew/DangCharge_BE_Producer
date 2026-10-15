package com.picka_chew.dangcharge.api.auth.dto;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
		@NotBlank @Size(min = 4, max = 50) @Pattern(regexp = "^[A-Za-z0-9_.-]+$", message = "영문, 숫자, _ . -만 쓸 수 있습니다")
		String loginId,
		@NotBlank @Size(max = 50) String username,
		@NotBlank String password,
		@NotNull VehicleModel vehicleModel) {
}
