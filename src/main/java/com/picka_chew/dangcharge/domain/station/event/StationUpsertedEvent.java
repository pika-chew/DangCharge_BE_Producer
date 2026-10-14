package com.picka_chew.dangcharge.domain.station.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/** 동기화로 새로 생기거나 값이 바뀐 충전소 */
public record StationUpsertedEvent(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels, BigDecimal latitude, BigDecimal longitude,
		Instant syncedAt) implements DomainEvent {

	public static StationUpsertedEvent of(Station station) {
		return new StationUpsertedEvent(station.getId(), station.getName(), station.getAddress(), station.getMetro(),
				station.getCity(), station.getRapidCount(), station.getSlowCount(),
				Set.copyOf(station.getSupportedModels()), station.getLatitude(), station.getLongitude(),
				station.getSyncedAt());
	}

	@Override
	public String aggregateType() {
		return "station";
	}

	@Override
	public Long aggregateId() {
		return stationId;
	}

	@Override
	public String eventType() {
		return "StationUpserted";
	}

}
