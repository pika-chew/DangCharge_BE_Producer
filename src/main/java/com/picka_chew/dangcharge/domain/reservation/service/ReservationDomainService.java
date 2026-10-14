package com.picka_chew.dangcharge.domain.reservation.service;

import java.time.Instant;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.user.entity.User;

public interface ReservationDomainService {

	/** 예상 요금은 계산할 데이터가 없어 비워 둔다 */
	Reservation create(User user, Charger charger, Instant startsAt, Instant endsAt);

	void cancel(Reservation reservation);

	void complete(Reservation reservation, Instant completedAt);

}
