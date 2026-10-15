package com.picka_chew.dangcharge.domain.reservation.service;

import java.time.Instant;

import com.picka_chew.dangcharge.common.annotation.DomainService;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.repository.ReservationRepository;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/** 트랜잭션은 호출하는 UseCase가 연다. 허용되지 않는 상태 전이는 엔티티가 막는다 */
@DomainService
@RequiredArgsConstructor
public class ReservationDomainServiceImpl implements ReservationDomainService {

	private final ReservationRepository reservationRepository;

	@Override
	public Reservation create(User user, Charger charger, Instant startsAt, Instant endsAt) {
		return reservationRepository.save(Reservation.builder()
				.user(user)
				.charger(charger)
				.startsAt(startsAt)
				.endsAt(endsAt)
				.vehicleModel(user.getVehicleModel())
				.build());
	}

	@Override
	public void cancel(Reservation reservation) {
		reservation.cancel();
	}

	@Override
	public void startCharging(Reservation reservation, Instant startedAt) {
		reservation.startCharging(startedAt);
	}

	@Override
	public void complete(Reservation reservation, Instant completedAt) {
		reservation.complete(completedAt);
	}

}
