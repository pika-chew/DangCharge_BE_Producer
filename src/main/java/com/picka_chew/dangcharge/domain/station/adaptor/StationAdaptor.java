package com.picka_chew.dangcharge.domain.station.adaptor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public interface StationAdaptor {

	/** 주어진 차종을 충전할 수 있는 충전소 */
	Page<Station> queryStationsSupporting(VehicleModel model, Pageable pageable);

	/** 주어진 시도·시군구 안에서 주어진 차종을 충전할 수 있는 충전소 */
	Page<Station> queryStationsSupportingIn(VehicleModel model, String metro, String city, Pageable pageable);

}
