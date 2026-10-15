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
import com.picka_chew.dangcharge.api.reservation.dto.MyReservationResponse;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationDetailResponse;
import com.picka_chew.dangcharge.api.reservation.service.CancelReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.CheckInUseCase;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.api.reservation.service.GetMyReservationUseCase;
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
	private final GetMyReservationUseCase getMyReservationUseCase;
	private final GetReservationUseCase getReservationUseCase;
	private final CancelReservationUseCase cancelReservationUseCase;
	private final CheckInUseCase checkInUseCase;

	@Operation(summary = "예약 생성",
			description = "사용자는 진행 중인 예약(예약·충전중)을 하나만 가질 수 있다. 이미 진행 중인 예약(예약, 충전중)이 있으면 새 예약을 만들지 않는다. "
					+ "모든 예약은 1시간이다. 시작 시각만 보내면 종료 시각은 1시간 뒤로 정해진다. "
					+ "시작은 정각 또는 30분, 오늘부터 7일 이내다. "
					+ "Idempotency-Key 헤더를 보내면 같은 키의 재요청에는 처음 만든 예약을 돌려준다.")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponseDto<ReservationDetailResponse> createReservation(@CurrentUser AuthUser authUser,
			@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
			@Valid @RequestBody CreateReservationRequest request) {
		return ApiResponseDto.onSuccess(createReservationUseCase.execute(authUser.userId(), request, idempotencyKey));
	}

	@Operation(summary = "내 예약 확인하기",
			description = "진행 중인 내 예약(예약·충전중)이 있는지와 ID, 시간, 충전소, 상태를 확인한다. "
					+ "사용자당 진행 중인 예약은 하나라 ID 없이 조회한다. 예약이 없으면 hasReservation만 false다.")
	@GetMapping("/me")
	public ApiResponseDto<MyReservationResponse> getMyReservation(@CurrentUser AuthUser authUser) {
		return ApiResponseDto.onSuccess(getMyReservationUseCase.execute(authUser.userId()));
	}

	@Operation(summary = "예약 상세확인하기",
			description = "예약 번호, 시간, 충전소, 예약 상태를 확인한다. 취소·완료된 예약도 내 예약이면 조회된다.")
	@GetMapping("/{reservationId}")
	public ApiResponseDto<ReservationDetailResponse> getReservation(@CurrentUser AuthUser authUser,
			@PathVariable Long reservationId) {
		return ApiResponseDto.onSuccess(getReservationUseCase.execute(authUser.userId(), reservationId));
	}

	@Operation(summary = "충전 시작",
			description = "예약(CONFIRMED)을 충전중(CHARGING)으로 바꾼다. 예약 시작 10분 전부터 시작 15분 후까지만 할 수 있다.")
	@PostMapping("/{reservationId}/check-in")
	public ApiResponseDto<ReservationDetailResponse> checkIn(@CurrentUser AuthUser authUser,
			@PathVariable Long reservationId) {
		return ApiResponseDto.onSuccess(checkInUseCase.execute(authUser.userId(), reservationId));
	}

	@Operation(summary = "예약 취소", description = "충전을 시작하기 전, 예약 시작 10분 전까지만 취소할 수 있다.")
	@DeleteMapping("/{reservationId}")
	public ApiResponseDto<Void> cancelReservation(@CurrentUser AuthUser authUser, @PathVariable Long reservationId) {
		cancelReservationUseCase.execute(authUser.userId(), reservationId);
		return ApiResponseDto.onSuccess(null);
	}

}
