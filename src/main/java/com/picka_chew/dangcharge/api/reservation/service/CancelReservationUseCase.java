package com.picka_chew.dangcharge.api.reservation.service;

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

@UseCase
@RequiredArgsConstructor
public class CancelReservationUseCase {

	private final ReservationAdaptor reservationAdaptor;
	private final ReservationValidator reservationValidator;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public void execute(Long userId, Long reservationId) {
		Reservation reservation = reservationAdaptor.getActiveByIdAndUserId(reservationId, userId);
		reservationValidator.validateCancelable(reservation, Instant.now(clock));
		reservationDomainService.cancel(reservation);
		eventRecorder.record(ReservationEvent.canceled(reservation));
	}

}
