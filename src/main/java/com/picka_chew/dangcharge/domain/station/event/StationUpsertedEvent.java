package com.picka_chew.dangcharge.domain.station.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargerDataSource;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/** 동기화로 새로 생기거나 값·충전기가 바뀐 충전소. consumer가 충전소 문서에 충전기까지 담을 수 있게 함께 보낸다 */
public record StationUpsertedEvent(Long stationId, String name, String address, String metro, String city,
		int rapidCount, int slowCount, Set<VehicleModel> supportedModels, BigDecimal latitude, BigDecimal longitude,
		Instant syncedAt, List<ChargerSnapshot> chargers) implements DomainEvent {

	public static StationUpsertedEvent of(Station station, Collection<Charger> chargers) {
		return new StationUpsertedEvent(station.getId(), station.getName(), station.getAddress(), station.getMetro(),
				station.getCity(), station.getRapidCount(), station.getSlowCount(),
				Set.copyOf(station.getSupportedModels()), station.getLatitude(), station.getLongitude(),
				station.getSyncedAt(), chargers.stream().map(ChargerSnapshot::of).toList());
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

	public record ChargerSnapshot(Long chargerId, ChargingType chargingType, BigDecimal maxPowerKw, OperationalStatus status, ChargerDataSource dataSource) {

		static ChargerSnapshot of(Charger charger) {
			return new ChargerSnapshot(charger.getId(), charger.getChargingType(), charger.getMaxPowerKw(), charger.getOperationalStatus(), charger.getDataSource());
		}

	}

}
