package com.picka_chew.dangcharge.api.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@Tag(name = "reservation", description = "[예약] 충전구 예약. 모든 API가 로그인을 요구한다")
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationApiController {

	@Operation(summary = "예약 생성",
			description = "사용자는 진행 중인 예약을 하나만 가질 수 있다. 이미 진행 중인 예약(예약, 충전중)이 있으면 새 예약을 만들지 않는다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDto<ReservationResponse> createReservation(@RequestBody CreateReservationRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	@Operation(summary = "예약 상세")
	@GetMapping("/{reservationId}")
	public ApiResponseDto<ReservationResponse> getReservation(@PathVariable Long reservationId) {
		// 조회 방식: 로그인 사용자의 현재 진행 중인 예약 내역을 조회한다. 진행 중은 예약, 충전중 두 상태다.
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	@Operation(summary = "예약 취소")
	@DeleteMapping("/{reservationId}")
	public ApiResponseDto<Void> cancelReservation(@PathVariable Long reservationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
