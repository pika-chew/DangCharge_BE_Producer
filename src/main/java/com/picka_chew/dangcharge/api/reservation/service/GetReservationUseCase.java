package com.picka_chew.dangcharge.api.reservation.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 로그인 사용자의 진행 중(CONFIRMED)인 예약만 조회한다. 끝난 예약은 충전 내역에서 본다 */
@UseCase
@RequiredArgsConstructor
public class GetReservationUseCase {

	private final ReservationAdaptor reservationAdaptor;

	@Transactional(readOnly = true)
	public ReservationResponse execute(Long userId, Long reservationId) {
		return ReservationResponse.from(reservationAdaptor.getActiveByIdAndUserId(reservationId, userId));
	}

}
