package com.picka_chew.dangcharge.domain.reservation.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;

@ExtendWith(MockitoExtension.class)
class ReservationValidatorImplTest {

	/** 2026-10-14 10:00 (KST) */
	private static final Instant NOW = kst("2026-10-14T10:00");

	@Mock
	ReservationAdaptor reservationAdaptor;
	@InjectMocks
	ReservationValidatorImpl validator;

	@Test
	void acceptsFutureSlotAlignedRangeWithinLimits() {
		assertThatCode(() -> validator.validateTimeRange(kst("2026-10-14T14:00"), kst("2026-10-14T18:00"), NOW))
				.doesNotThrowAnyException();
		assertThatCode(() -> validator.validateTimeRange(kst("2026-10-21T23:30"), kst("2026-10-22T00:00"), NOW))
				.doesNotThrowAnyException();
	}

	@ParameterizedTest(name = "{2}")
	@CsvSource({
			"2026-10-14T09:30, 2026-10-14T10:30, 이미 지난 시작",
			"2026-10-14T10:00, 2026-10-14T10:30, 지금 시작",
			"2026-10-14T14:10, 2026-10-14T15:00, 30분 단위가 아닌 시작",
			"2026-10-14T14:00, 2026-10-14T14:45, 30분 단위가 아닌 종료",
			"2026-10-14T15:00, 2026-10-14T14:00, 종료가 시작보다 앞",
			"2026-10-14T14:00, 2026-10-14T14:00, 길이 0",
			"2026-10-14T14:00, 2026-10-14T18:30, 4시간 초과",
			"2026-10-22T00:00, 2026-10-22T00:30, 7일 뒤 날짜"
	})
	void rejectsInvalidRange(String start, String end, String reason) {
		assertThatThrownBy(() -> validator.validateTimeRange(kst(start), kst(end), NOW))
				.isInstanceOf(GeneralException.class)
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RESERVATION_TIME);
	}

	@Test
	void cancelAllowedUntilTenMinutesBeforeStart() {
		Reservation reservation = Reservation.builder().startsAt(kst("2026-10-14T10:30")).build();

		assertThatCode(() -> validator.validateCancelable(reservation, kst("2026-10-14T10:20")))
				.doesNotThrowAnyException();
		assertThatThrownBy(() -> validator.validateCancelable(reservation, kst("2026-10-14T10:21")))
				.extracting("errorStatus").isEqualTo(ErrorStatus.RESERVATION_NOT_CANCELABLE);
	}

	@Test
	void stopAllowedOnlyAfterStart() {
		Reservation reservation = Reservation.builder().startsAt(kst("2026-10-14T10:30")).build();

		assertThatThrownBy(() -> validator.validateStoppable(reservation, kst("2026-10-14T10:29")))
				.extracting("errorStatus").isEqualTo(ErrorStatus.RESERVATION_NOT_STARTED);
		assertThatCode(() -> validator.validateStoppable(reservation, kst("2026-10-14T10:30")))
				.doesNotThrowAnyException();
	}

	static Instant kst(String localDateTime) {
		return OffsetDateTime.parse(localDateTime + ":00+09:00").toInstant();
	}

}
