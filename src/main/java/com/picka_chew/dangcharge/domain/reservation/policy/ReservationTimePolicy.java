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
	/** 예약 시작 시각 단위. 정각 또는 30분에 시작한다 */
	public static final Duration SLOT = Duration.ofMinutes(30);
	/** 예약 한 건의 이용 시간. 사용자가 고르지 않고 시작 시각에서 서버가 종료 시각을 정한다 */
	public static final Duration DURATION = Duration.ofHours(1);
	/** 오늘부터 며칠 뒤까지 예약할 수 있는지 */
	public static final int MAX_DAYS_AHEAD = 7;
	/** 시작 몇 분 전까지 취소할 수 있는지 */
	public static final Duration CANCEL_DEADLINE = Duration.ofMinutes(10);
	/** 예약 시작 몇 분 전부터 충전을 시작할 수 있는지 */
	public static final Duration CHECK_IN_OPENS_BEFORE = Duration.ofMinutes(10);
	/** 예약 시작 몇 분 후까지 충전을 시작할 수 있는지 */
	public static final Duration CHECK_IN_CLOSES_AFTER = Duration.ofMinutes(15);

	private ReservationTimePolicy() {
	}

	public static Instant endOf(Instant startsAt) {
		return startsAt.plus(DURATION);
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
	 * 그날 시작할 수 있는 예약(30분 간격 시작, 1시간 이용) 중 충전기별로 비어 있는 것.
	 * 이미 지난 시작 시각과 쓸 수 없는 충전기는 뺀다. 23:30 시작은 다음 날 00:30에 끝난다.
	 *
	 * @param reservations 그날 00:00부터 다음 날 01:00까지와 겹치는 CONFIRMED 예약
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
				Instant end = endOf(start);
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
