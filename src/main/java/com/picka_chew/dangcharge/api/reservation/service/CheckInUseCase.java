package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.reservation.dto.ReservationDetailResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.event.ReservationEvent;
import com.picka_chew.dangcharge.domain.reservation.service.ReservationDomainService;
import com.picka_chew.dangcharge.domain.reservation.validator.ReservationValidator;

import lombok.RequiredArgsConstructor;

/** 충전 시작. 예약(CONFIRMED)을 충전중(CHARGING)으로 바꾼다. 예약 상태가 아니면 INVALID_RESERVATION_STATUS */
@UseCase
@RequiredArgsConstructor
public class CheckInUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final ReservationValidator reservationValidator;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public ReservationDetailResponse execute(Long userId, Long reservationId) {
		Instant now = Instant.now(clock);
		Reservation reservation = reservationAdaptor.getByIdAndUserId(reservationId, userId);
		reservationValidator.validateCheckInWindow(reservation, now);
		reservationDomainService.startCharging(reservation, now);
		eventRecorder.record(ReservationEvent.chargingStarted(reservation));
		return ReservationDetailResponse.from(reservation);
	}

}
