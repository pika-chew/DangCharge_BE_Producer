package com.picka_chew.dangcharge.api.station.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.picka_chew.dangcharge.domain.reservation.model.ReservationSlot;

import tools.jackson.databind.json.JsonMapper;

class AvailableSlotResponseTest {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void statusIsWrittenAsEnableOrDisable() {
		Instant start = Instant.parse("2026-10-16T01:00:00Z");
		Instant end = Instant.parse("2026-10-16T02:00:00Z");

		String free = jsonMapper.writeValueAsString(AvailableSlotResponse.from(new ReservationSlot(1L, start, end, false)));
		String reserved = jsonMapper.writeValueAsString(AvailableSlotResponse.from(new ReservationSlot(1L, start, end, true)));

		assertThat(free).contains("\"status\":\"enable\"");
		assertThat(reserved).contains("\"status\":\"disable\"");
	}

}
