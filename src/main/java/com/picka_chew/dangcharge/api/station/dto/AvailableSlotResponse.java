package com.picka_chew.dangcharge.api.station.dto;

import java.time.OffsetDateTime;

import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.reservation.model.AvailableSlot;

public record AvailableSlotResponse(Long chargerId, OffsetDateTime startAt, OffsetDateTime endAt) {

	public static AvailableSlotResponse from(AvailableSlot slot) {
		return new AvailableSlotResponse(slot.chargerId(), TimeUtils.toOffsetDateTime(slot.startsAt()),
				TimeUtils.toOffsetDateTime(slot.endsAt()));
	}

}
