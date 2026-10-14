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
	void acceptsFutureStartOnHourOrHalfHourWithinSevenDays() {
		assertThatCode(() -> validator.validateStartTime(kst("2026-10-14T10:30"), NOW)).doesNotThrowAnyException();
		assertThatCode(() -> validator.validateStartTime(kst("2026-10-21T23:30"), NOW)).doesNotThrowAnyException();
	}

	@ParameterizedTest(name = "{1}")
	@CsvSource({
			"2026-10-14T09:30, 이미 지난 시작",
			"2026-10-14T10:00, 지금 시작",
			"2026-10-14T14:10, 정각이나 30분이 아닌 시작",
			"2026-10-22T00:00, 7일 뒤 날짜"
	})
	void rejectsInvalidStart(String start, String reason) {
		assertThatThrownBy(() -> validator.validateStartTime(kst(start), NOW))
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
