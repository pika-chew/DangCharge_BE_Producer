package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.event.ReservationEvent;
import com.picka_chew.dangcharge.domain.reservation.service.ReservationDomainService;

import lombok.RequiredArgsConstructor;

/**
 * 충전 시작 마감(예약 시작 15분 후)이 지나도록 충전을 시작하지 않은 예약을 NO_SHOW로 바꾼다.
 * 그러면 사용자는 새 예약을 할 수 있고, 충전기의 남은 시간도 다시 예약할 수 있게 된다. 패널티는 없다.
 */
@UseCase
@RequiredArgsConstructor
public class MarkNoShowReservationsUseCase {

	static final int BATCH_SIZE = 100;

	private final ReservationAdaptor reservationAdaptor;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	/** @return 이번에 NO_SHOW로 바꾼 예약 수 */
	@Transactional
	public int execute() {
		List<Reservation> targets = reservationAdaptor.findNoShowTargetsForUpdate(Instant.now(clock), BATCH_SIZE);
		for (Reservation reservation : targets) {
			reservationDomainService.markNoShow(reservation);
			eventRecorder.record(ReservationEvent.noShow(reservation));
		}
		return targets.size();
	}

}
