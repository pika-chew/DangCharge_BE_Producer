package com.picka_chew.dangcharge.api.reservation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.charging.dto.ChargingSessionResponse;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.entity.ReservationStatus;

/** 모든 API가 로그인을 요구한다 */
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationApiController {

	/** 예약 생성 */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDto<ReservationResponse> createReservation(@RequestBody CreateReservationRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 내 예약 목록 */
	@GetMapping
	public ApiResponseDto<List<ReservationResponse>> getReservations(
			@RequestParam(required = false) ReservationStatus status) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 예약 상세 */
	@GetMapping("/{reservationId}")
	public ApiResponseDto<ReservationResponse> getReservation(@PathVariable Long reservationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 예약 취소 */
	@DeleteMapping("/{reservationId}")
	public ApiResponseDto<Void> cancelReservation(@PathVariable Long reservationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전 시작. 충전 세션을 만든다 */
	@PostMapping("/{reservationId}/check-in")
	public ApiResponseDto<ChargingSessionResponse> checkIn(@PathVariable Long reservationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
