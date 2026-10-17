package com.picka_chew.dangcharge.domain.reservation.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.picka_chew.dangcharge.common.exception.ErrorStatus;

class ReservationTest {

	@Test
	void reservationNumberUsesKoreanCreationDateAndPaddedId() {
		Reservation reservation = Reservation.builder().id(42L).build();
		// 2026-10-14 23:30 UTC는 한국 시간으로 10월 15일이다
		ReflectionTestUtils.setField(reservation, "createdAt", Instant.parse("2026-10-14T23:30:00Z"));

		assertThat(reservation.getReservationNumber()).isEqualTo("DC20261015-000042");
	}

	@Test
	void reservationMovesFromConfirmedToChargingToCompleted() {
		Reservation reservation = Reservation.builder().build();
		Instant started = Instant.parse("2026-10-15T01:00:00Z");
		Instant completed = Instant.parse("2026-10-15T02:00:00Z");

		reservation.startCharging(started);
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CHARGING);
		assertThat(reservation.getChargingStartedAt()).isEqualTo(started);

		reservation.complete(completed);
		assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
		assertThat(reservation.getCompletedAt()).isEqualTo(completed);
	}

	@Test
	void cannotStopBeforeStartingOrCancelWhileCharging() {
		Reservation confirmed = Reservation.builder().build();
		assertThatThrownBy(() -> confirmed.complete(Instant.now()))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RESERVATION_STATUS);

		Reservation charging = Reservation.builder().build();
		charging.startCharging(Instant.now());
		assertThatThrownBy(charging::cancel)
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RESERVATION_STATUS);
		assertThatThrownBy(() -> charging.startCharging(Instant.now()))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RESERVATION_STATUS);
	}

	@Test
	void onlyConfirmedReservationBecomesNoShow() {
		Reservation confirmed = Reservation.builder().build();
		confirmed.markNoShow();
		assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);

		Reservation charging = Reservation.builder().build();
		charging.startCharging(Instant.now());
		assertThatThrownBy(charging::markNoShow)
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RESERVATION_STATUS);
	}

}
