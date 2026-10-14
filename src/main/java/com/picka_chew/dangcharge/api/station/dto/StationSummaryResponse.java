package com.picka_chew.dangcharge.api.station.dto;

import com.picka_chew.dangcharge.domain.station.entity.Station;

public record StationSummaryResponse(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount) {

	public static StationSummaryResponse from(Station station) {
		return new StationSummaryResponse(station.getId(), station.getName(), station.getAddress(), station.getMetro(),
				station.getCity(), station.getRapidCount(), station.getSlowCount());
	}

}
