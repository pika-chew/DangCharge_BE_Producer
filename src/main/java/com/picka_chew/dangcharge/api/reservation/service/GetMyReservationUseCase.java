package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.reservation.dto.MyReservationResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 사용자당 진행 중인 예약은 하나라서 ID 없이 조회한다 */
@UseCase
@RequiredArgsConstructor
public class GetMyReservationUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final Clock clock;

	@Transactional(readOnly = true)
	public MyReservationResponse execute(Long userId) {
		return reservationAdaptor.findActiveByUserId(userId, Instant.now(clock))
				.map(MyReservationResponse::from)
				.orElseGet(MyReservationResponse::none);
	}

}
