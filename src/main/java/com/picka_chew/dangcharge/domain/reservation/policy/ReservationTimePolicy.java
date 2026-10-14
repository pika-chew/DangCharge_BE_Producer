package com.picka_chew.dangcharge.domain.reservation.policy;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.model.AvailableSlot;
import com.picka_chew.dangcharge.domain.station.entity.Charger;

/**
 * 예약 시간 규칙. 충전소 운영시간 데이터가 없어 하루 24시간을 예약 가능 시간으로 본다.
 * 날짜·슬롯 경계는 한국 시간 기준이다.
 */
public final class ReservationTimePolicy {

	public static final ZoneId ZONE = TimeUtils.ZONE;
	/** 예약 단위. 시작·종료 시각은 이 단위로 맞아야 한다 */
	public static final Duration SLOT = Duration.ofMinutes(30);
	public static final Duration MAX_DURATION = Duration.ofHours(4);
	/** 오늘부터 며칠 뒤까지 예약할 수 있는지 */
	public static final int MAX_DAYS_AHEAD = 7;
	/** 시작 몇 분 전까지 취소할 수 있는지 */
	public static final Duration CANCEL_DEADLINE = Duration.ofMinutes(10);

	private ReservationTimePolicy() {
	}

	public static boolean isSlotAligned(Instant instant) {
		ZonedDateTime time = instant.atZone(ZONE);
		return time.getMinute() % SLOT.toMinutes() == 0 && time.getSecond() == 0 && time.getNano() == 0;
	}

	/** now 기준으로 예약할 수 있는 날짜인지. 오늘부터 MAX_DAYS_AHEAD일 뒤까지 */
	public static boolean isBookableDate(LocalDate date, Instant now) {
		LocalDate today = LocalDate.ofInstant(now, ZONE);
		return !date.isBefore(today) && !date.isAfter(today.plusDays(MAX_DAYS_AHEAD));
	}

	public static Instant startOfDay(LocalDate date) {
		return date.atStartOfDay(ZONE).toInstant();
	}

	/**
	 * 날짜의 슬롯 중 충전기별로 비어 있는 것. 이미 지난 슬롯과 쓸 수 없는 충전기는 뺀다.
	 *
	 * @param reservations 그날과 겹치는 CONFIRMED 예약
	 */
	public static List<AvailableSlot> availableSlots(Collection<Charger> chargers,
			Collection<Reservation> reservations, LocalDate date, Instant now) {
		Instant dayStart = startOfDay(date);
		Instant dayEnd = startOfDay(date.plusDays(1));
		List<AvailableSlot> slots = new ArrayList<>();
		for (Charger charger : chargers) {
			if (!charger.isUsable()) {
				continue;
			}
			for (Instant start = dayStart; start.isBefore(dayEnd); start = start.plus(SLOT)) {
				Instant end = start.plus(SLOT);
				if (start.isBefore(now) || overlapsAny(charger.getId(), start, end, reservations)) {
					continue;
				}
				slots.add(new AvailableSlot(charger.getId(), start, end));
			}
		}
		return slots;
	}

	private static boolean overlapsAny(Long chargerId, Instant start, Instant end,
			Collection<Reservation> reservations) {
		for (Reservation reservation : reservations) {
			if (reservation.getCharger().getId().equals(chargerId)
					&& reservation.getStartsAt().isBefore(end) && reservation.getEndsAt().isAfter(start)) {
				return true;
			}
		}
		return false;
	}

}
