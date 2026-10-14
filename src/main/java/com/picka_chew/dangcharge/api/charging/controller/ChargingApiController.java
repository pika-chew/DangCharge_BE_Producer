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
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.common.dto.PagedResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@Tag(name = "charging", description = "[충전] 충전 종료·충전 내역")
@RestController
@RequestMapping("/api/v1/charging")
public class ChargingApiController {

	@Operation(summary = "충전 종료", description = "예약을 COMPLETED로 바꾼다. 로그인이 필요하다.")
	@PostMapping("/{sessionId}/stop")
	public ApiResponseDto<Void> stopSession(@PathVariable Long sessionId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	@Operation(summary = "충전 내역 목록", description = "기간 조건 없이 전체 내역을 페이지 단위로 조회한다. 로그인이 필요하다.")
	@GetMapping("/history")
	public ApiResponseDto<PagedResponse<ChargingHistoryResponse>> getHistory(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	@Operation(summary = "충전 내역 상세", description = "로그인이 필요하다.")
	@GetMapping("/history/{sessionId}")
	public ApiResponseDto<ChargingHistoryResponse> getHistoryDetail(@PathVariable Long sessionId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
