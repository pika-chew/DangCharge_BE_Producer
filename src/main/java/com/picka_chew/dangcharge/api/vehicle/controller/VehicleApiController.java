package com.picka_chew.dangcharge.api.vehicle.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.vehicle.dto.VehicleModelResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@RestController
@RequestMapping("/api/v1/vehicle-models")
public class VehicleApiController {

	/** 차종 목록 */
	@GetMapping
	public ApiResponseDto<List<VehicleModelResponse>> getVehicleModels() {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
