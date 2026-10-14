package com.picka_chew.dangcharge.api.admin.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.admin.dto.StationSyncResponse;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.station.service.SyncStationsUseCase;
import com.picka_chew.dangcharge.common.config.SwaggerConfig;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;

import lombok.RequiredArgsConstructor;

@Tag(name = "admin", description = "[관리] 내부·관리용 API. X-Admin-Key 헤더가 필요하다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminApiController {

	private final SyncStationsUseCase syncStationsUseCase;

	@Operation(summary = "충전소 데이터 동기화",
			description = "외부 충전소 설치현황을 DB로 동기화한다. 스케줄러 배치와 같은 UseCase를 쓴다.")
	@SecurityRequirement(name = SwaggerConfig.ADMIN_KEY_SCHEME)
	@PostMapping("/stations/sync")
	public ApiResponseDto<StationSyncResponse> syncStations() {
		StationSyncResult result = syncStationsUseCase.execute();
		return ApiResponseDto.onSuccess(new StationSyncResponse(result.created(), result.updated()));
	}

}
