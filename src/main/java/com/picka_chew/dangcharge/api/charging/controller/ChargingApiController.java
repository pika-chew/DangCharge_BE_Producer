package com.picka_chew.dangcharge.api.charging.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge.api.charging.service.GetChargingHistoriesUseCase;
import com.picka_chew.dangcharge.api.charging.service.GetChargingHistoryUseCase;
import com.picka_chew.dangcharge.api.charging.service.StopChargingUseCase;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.common.dto.PagedResponse;
import com.picka_chew.dangcharge.common.annotation.CurrentUser;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/** 충전 세션 테이블이 없어 sessionId는 예약 ID다 */
@Tag(name = "charging", description = "[충전] 충전 종료·충전 내역")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/charging")
public class ChargingApiController {

	private final StopChargingUseCase stopChargingUseCase;
	private final GetChargingHistoriesUseCase getChargingHistoriesUseCase;
	private final GetChargingHistoryUseCase getChargingHistoryUseCase;

	@Operation(summary = "충전 종료", description = "예약을 COMPLETED로 바꾼다. sessionId는 예약 ID다. 예약 시작 이후에만 할 수 있다. 로그인이 필요하다.")
	@PostMapping("/{sessionId}/stop")
	public ApiResponseDto<Void> stopSession(@CurrentUser AuthUser authUser, @PathVariable Long sessionId) {
		stopChargingUseCase.execute(authUser.userId(), sessionId);
		return ApiResponseDto.onSuccess(null);
	}

	@Operation(summary = "충전 내역 목록", description = "기간 조건 없이 전체 내역을 최근 완료 순으로 페이지 단위로 조회한다. 로그인이 필요하다.")
	@GetMapping("/history")
	public ApiResponseDto<PagedResponse<ChargingHistoryResponse>> getHistory(
			@CurrentUser AuthUser authUser,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return ApiResponseDto.onSuccess(getChargingHistoriesUseCase.execute(authUser.userId(), page, size));
	}

	@Operation(summary = "충전 내역 상세", description = "sessionId는 예약 ID다. 로그인이 필요하다.")
	@GetMapping("/history/{sessionId}")
	public ApiResponseDto<ChargingHistoryResponse> getHistoryDetail(@CurrentUser AuthUser authUser,
			@PathVariable Long sessionId) {
		return ApiResponseDto.onSuccess(getChargingHistoryUseCase.execute(authUser.userId(), sessionId));
	}

}
