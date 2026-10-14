package com.picka_chew.dangcharge.api.charging.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.event.ReservationEvent;
import com.picka_chew.dangcharge.domain.reservation.service.ReservationDomainService;
import com.picka_chew.dangcharge.domain.reservation.validator.ReservationValidator;

import lombok.RequiredArgsConstructor;

/**
 * 충전 종료. 충전 세션 테이블이 없어 sessionId로 예약 ID를 받고, 예약을 COMPLETED로 바꾼다.
 * 충전량·요금은 계산하지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class StopChargingUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final ReservationValidator reservationValidator;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public void execute(Long userId, Long sessionId) {
		Instant now = Instant.now(clock);
		Reservation reservation = reservationAdaptor.getActiveByIdAndUserId(sessionId, userId);
		reservationValidator.validateStoppable(reservation, now);
		reservationDomainService.complete(reservation, now);
		eventRecorder.record(ReservationEvent.completed(reservation));
	}

}
