package com.picka_chew.dangcharge.api.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.api.reservation.service.CancelReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.GetReservationUseCase;
import com.picka_chew.dangcharge.common.annotation.CurrentUser;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "reservation", description = "[예약] 충전구 예약. 모든 API가 로그인을 요구한다")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationApiController {

	private final CreateReservationUseCase createReservationUseCase;
	private final GetReservationUseCase getReservationUseCase;
	private final CancelReservationUseCase cancelReservationUseCase;

	@Operation(summary = "예약 생성",
			description = "사용자는 진행 중인 예약을 하나만 가질 수 있다. 이미 진행 중인 예약(예약, 충전중)이 있으면 새 예약을 만들지 않는다. "
					+ "시작·종료는 30분 단위, 최대 4시간, 오늘부터 7일 이내다. "
					+ "Idempotency-Key 헤더를 보내면 같은 키의 재요청에는 처음 만든 예약을 돌려준다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDto<ReservationResponse> createReservation(@CurrentUser AuthUser authUser,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CreateReservationRequest request) {
		return ApiResponseDto.onSuccess(createReservationUseCase.execute(authUser.userId(), request, idempotencyKey));
	}

	@Operation(summary = "예약 상세")
	@GetMapping("/{reservationId}")
	public ApiResponseDto<ReservationResponse> getReservation(@CurrentUser AuthUser authUser,
			@PathVariable Long reservationId) {
		// 조회 방식: 로그인 사용자의 현재 진행 중인 예약 내역을 조회한다. 진행 중은 예약, 충전중 두 상태다.
		return ApiResponseDto.onSuccess(getReservationUseCase.execute(authUser.userId(), reservationId));
	}

	@Operation(summary = "예약 취소", description = "시작 10분 전까지만 취소할 수 있다.")
	@DeleteMapping("/{reservationId}")
	public ApiResponseDto<Void> cancelReservation(@CurrentUser AuthUser authUser, @PathVariable Long reservationId) {
		cancelReservationUseCase.execute(authUser.userId(), reservationId);
		return ApiResponseDto.onSuccess(null);
	}

}
