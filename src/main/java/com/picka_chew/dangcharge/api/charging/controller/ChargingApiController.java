package com.picka_chew.dangcharge.api.charging.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge.api.charging.dto.ChargingSessionResponse;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.common.dto.PagedResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

/** 모든 API가 로그인을 요구한다 */
@RestController
@RequestMapping("/api/v1/charging")
public class ChargingApiController {

	/** 진행 중 세션 */
	@GetMapping("/current")
	public ApiResponseDto<ChargingSessionResponse> getCurrentSession() {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전 종료. 최종 요금을 확정한다 */
	@PostMapping("/{sessionId}/stop")
	public ApiResponseDto<ChargingHistoryResponse> stopSession(@PathVariable Long sessionId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전 내역 목록 */
	@GetMapping("/history")
	public ApiResponseDto<PagedResponse<ChargingHistoryResponse>> getHistory(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전 내역 상세 */
	@GetMapping("/history/{sessionId}")
	public ApiResponseDto<ChargingHistoryResponse> getHistoryDetail(@PathVariable Long sessionId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
