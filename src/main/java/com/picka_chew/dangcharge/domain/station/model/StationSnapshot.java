package com.picka_chew.dangcharge.domain.station.model;

import java.util.EnumSet;
import java.util.Set;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/**
 * 외부 설치현황에서 읽은 충전소 한 건. 아직 저장되지 않은 값이다.
 */
public record StationSnapshot(
		String metro,
		String city,
		String name,
		String address,
		int rapidCount,
		int slowCount,
		Set<VehicleModel> supportedModels) {

	/**
	 * 같은 충전소가 여러 행으로 나뉘어 온 경우 하나로 합친다. 충전기 수는 더하고 지원 차종은 합집합으로 한다.
	 */
	public StationSnapshot merge(StationSnapshot other) {
		Set<VehicleModel> models = EnumSet.noneOf(VehicleModel.class);
		models.addAll(supportedModels);
		models.addAll(other.supportedModels);
		return new StationSnapshot(metro, city, name, address,
				rapidCount + other.rapidCount, slowCount + other.slowCount, models);
	}

}
