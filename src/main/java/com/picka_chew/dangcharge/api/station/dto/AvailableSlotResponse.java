package com.picka_chew.dangcharge.api.station.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonValue;
import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.model.ReservationSlot;

/**
 * 충전기 하나의 예약 시간대.
 *
 * @param status enable: 예약할 수 있음, disable: 이미 예약된 시간대
 */
public record AvailableSlotResponse(Long chargerId, OffsetDateTime startAt, OffsetDateTime endAt, SlotStatus status) {

	public static AvailableSlotResponse from(ReservationSlot slot) {
		return new AvailableSlotResponse(slot.chargerId(), TimeUtils.toOffsetDateTime(slot.startsAt()),
				TimeUtils.toOffsetDateTime(slot.endsAt()), slot.reserved() ? SlotStatus.DISABLE : SlotStatus.ENABLE);
	}

	public enum SlotStatus {

		ENABLE("enable"),
		DISABLE("disable");

		private final String value;

		SlotStatus(String value) {
			this.value = value;
		}

		@JsonValue
		public String value() {
			return value;
		}

	}

}
