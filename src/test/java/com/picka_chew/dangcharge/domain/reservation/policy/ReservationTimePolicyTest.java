package com.picka_chew.dangcharge.domain.reservation.policy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.model.ReservationSlot;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;

class ReservationTimePolicyTest {

	private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

	@Test
	void everyHalfHourStartsOneHourReservationInKoreanTime() {
		List<ReservationSlot> slots = ReservationTimePolicy.slotsOf(List.of(charger(1L, OperationalStatus.AVAILABLE)),
				List.of(), DATE, kst("2026-10-14T10:00"));

		assertThat(slots).hasSize(48);
		assertThat(slots.get(0).startsAt()).isEqualTo(kst("2026-10-15T00:00"));
		assertThat(slots.get(0).endsAt()).isEqualTo(kst("2026-10-15T01:00"));
		assertThat(slots.get(47).startsAt()).isEqualTo(kst("2026-10-15T23:30"));
		assertThat(slots.get(47).endsAt()).isEqualTo(kst("2026-10-16T00:30"));
	}

	@Test
	void marksReservedSlotsAndDropsPastSlotsAndBrokenChargers() {
		Charger usable = charger(1L, OperationalStatus.UNKNOWN);
		Charger broken = charger(2L, OperationalStatus.OUT_OF_SERVICE);
		Reservation reservation = Reservation.builder()
				.charger(usable).startsAt(kst("2026-10-15T12:00")).endsAt(kst("2026-10-15T13:00")).build();

		List<ReservationSlot> slots = ReservationTimePolicy.slotsOf(List.of(usable, broken),
				List.of(reservation), DATE, kst("2026-10-15T11:10"));

		assertThat(slots).allMatch(slot -> slot.chargerId().equals(1L));
		assertThat(slots).extracting(ReservationSlot::startsAt).doesNotContain(kst("2026-10-15T11:00"));
		// 12:00~13:00 예약과 겹치는 11:30, 12:00, 12:30 시작은 예약됨으로 표시된다
		assertThat(slots).filteredOn(ReservationSlot::reserved).extracting(ReservationSlot::startsAt)
				.containsExactly(kst("2026-10-15T11:30"), kst("2026-10-15T12:00"), kst("2026-10-15T12:30"));
		assertThat(slots).filteredOn(slot -> slot.startsAt().equals(kst("2026-10-15T13:00")))
				.singleElement().extracting(ReservationSlot::reserved).isEqualTo(false);
	}

	@Test
	void bookableDatesAreTodayToSevenDaysAhead() {
		Instant now = kst("2026-10-14T23:50");

		assertThat(ReservationTimePolicy.isBookableDate(LocalDate.of(2026, 10, 13), now)).isFalse();
		assertThat(ReservationTimePolicy.isBookableDate(LocalDate.of(2026, 10, 14), now)).isTrue();
		assertThat(ReservationTimePolicy.isBookableDate(LocalDate.of(2026, 10, 21), now)).isTrue();
		assertThat(ReservationTimePolicy.isBookableDate(LocalDate.of(2026, 10, 22), now)).isFalse();
	}

	private static Charger charger(Long id, OperationalStatus status) {
		return Charger.builder().id(id).operationalStatus(status).build();
	}

	private static Instant kst(String localDateTime) {
		return OffsetDateTime.parse(localDateTime + ":00+09:00").toInstant();
	}

}
