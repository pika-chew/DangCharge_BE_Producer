package com.picka_chew.dangcharge.api.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.admin.dto.StationSyncResponse;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@Tag(name = "admin", description = "[관리] 내부·관리용 API")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminApiController {

	@Operation(summary = "충전소 데이터 동기화", description = "외부 공공 API의 충전소 데이터를 DB로 동기화한다. 스케줄러 배치가 호출한다.")
	@PostMapping("/stations/sync")
	public ApiResponseDto<StationSyncResponse> syncStations() {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
