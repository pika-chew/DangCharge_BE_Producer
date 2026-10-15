package com.picka_chew.dangcharge.domain.reservation.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class ReservationTest {

	@Test
	void reservationNumberUsesKoreanCreationDateAndPaddedId() {
		Reservation reservation = Reservation.builder().id(42L).build();
		// 2026-10-14 23:30 UTC는 한국 시간으로 10월 15일이다
		ReflectionTestUtils.setField(reservation, "createdAt", Instant.parse("2026-10-14T23:30:00Z"));

		assertThat(reservation.getReservationNumber()).isEqualTo("DC20261015-000042");
	}

}
