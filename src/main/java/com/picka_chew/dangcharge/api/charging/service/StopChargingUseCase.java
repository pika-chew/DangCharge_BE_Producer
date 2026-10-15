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

import lombok.RequiredArgsConstructor;

/**
 * 충전 종료. 충전중(CHARGING)인 예약을 완료(COMPLETED)로 바꾼다. 충전중이 아니면 INVALID_RESERVATION_STATUS.
 * 충전량·요금은 계산하지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class StopChargingUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public void execute(Long userId, Long reservationId) {
		Reservation reservation = reservationAdaptor.getByIdAndUserId(reservationId, userId);
		reservationDomainService.complete(reservation, Instant.now(clock));
		eventRecorder.record(ReservationEvent.completed(reservation));
	}

}
