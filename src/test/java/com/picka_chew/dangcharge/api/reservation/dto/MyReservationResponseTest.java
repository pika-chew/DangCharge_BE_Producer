package com.picka_chew.dangcharge.api.reservation.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;

class MyReservationResponseTest {

	@Test
	void stateFollowsReservationStatus() {
		Station station = Station.builder().id(1L).name("강남 충전소").address("서울특별시 강남구").build();
		Reservation reservation = Reservation.builder().id(4L).charger(Charger.builder().id(2L).station(station).build())
				.startsAt(Instant.parse("2026-10-15T01:00:00Z")).endsAt(Instant.parse("2026-10-15T02:00:00Z")).build();

		MyReservationResponse reserved = MyReservationResponse.from(reservation);
		assertThat(reserved.state()).isEqualTo(MyReservationResponse.State.RESERVED);
		assertThat(reserved.message()).isEqualTo("예약되어 있습니다");
		assertThat(reserved.reservation().stationName()).isEqualTo("강남 충전소");

		reservation.startCharging(Instant.parse("2026-10-15T01:00:00Z"));
		MyReservationResponse charging = MyReservationResponse.from(reservation);
		assertThat(charging.state()).isEqualTo(MyReservationResponse.State.CHARGING);
		assertThat(charging.message()).isEqualTo("충전 중입니다");

		MyReservationResponse none = MyReservationResponse.none();
		assertThat(none.state()).isEqualTo(MyReservationResponse.State.NONE);
		assertThat(none.reservation()).isNull();
	}

}
