package com.picka_chew.dangcharge.api.station.dto;

import java.math.BigDecimal;

import com.picka_chew.dangcharge.domain.station.entity.Station;

/** @param distanceMeters 요청 좌표에서 충전소까지 거리(m) */
public record StationMarkerResponse(Long stationId, String name, BigDecimal latitude, BigDecimal longitude,
		int availableChargerCount, long distanceMeters) {

	public static StationMarkerResponse of(Station station, int availableChargerCount, long distanceMeters) {
		return new StationMarkerResponse(station.getId(), station.getName(), station.getLatitude(),
				station.getLongitude(), availableChargerCount, distanceMeters);
	}

}
