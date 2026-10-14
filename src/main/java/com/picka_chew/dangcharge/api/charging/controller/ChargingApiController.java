package com.picka_chew.dangcharge.api.charging.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@RestController
@RequestMapping("/api/v1/charging")
public class ChargingApiController {

	/** 충전 종료. 예약을 COMPLETED로 바꾼다. 로그인 필요 */
	@PostMapping("/{sessionId}/stop")
	public ApiResponseDto<Void> stopSession(@PathVariable Long sessionId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
