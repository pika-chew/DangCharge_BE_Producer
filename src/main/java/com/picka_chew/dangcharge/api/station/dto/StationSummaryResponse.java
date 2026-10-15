package com.picka_chew.dangcharge.api.station.dto;

import java.util.EnumSet;
import java.util.Set;

import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public record StationSummaryResponse(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels) {

	public static StationSummaryResponse from(Station station) {
		Set<VehicleModel> models = station.getSupportedModels().isEmpty()
				? EnumSet.noneOf(VehicleModel.class)
				: EnumSet.copyOf(station.getSupportedModels());
		return new StationSummaryResponse(station.getId(), station.getName(), station.getAddress(), station.getMetro(),
				station.getCity(), station.getRapidCount(), station.getSlowCount(), models);
	}

}
