package com.picka_chew.dangcharge.api.vehicle.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.vehicle.dto.VehicleModelResponse;
import com.picka_chew.dangcharge.api.vehicle.service.GetVehicleModelsUseCase;

import lombok.RequiredArgsConstructor;

@Tag(name = "vehicle", description = "[차종] 차종 목록")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vehicle-models")
public class VehicleApiController {

	private final GetVehicleModelsUseCase getVehicleModelsUseCase;

	@Operation(summary = "차종 목록")
	@SecurityRequirements
	@GetMapping
	public ApiResponseDto<List<VehicleModelResponse>> getVehicleModels() {
		return ApiResponseDto.onSuccess(getVehicleModelsUseCase.execute());
	}

}
