package com.picka_chew.dangcharge.domain.station.adaptor;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public interface StationAdaptor {

	/** 주어진 차종을 충전할 수 있는 충전소 */
	Page<Station> queryStationsSupporting(VehicleModel model, Pageable pageable);

	/** 주어진 시도·시군구 안에서 주어진 차종을 충전할 수 있는 충전소 */
	Page<Station> queryStationsSupportingIn(VehicleModel model, String metro, String city, Pageable pageable);

	/** 지원 차종까지 함께 조회한다. 없으면 STATION_NOT_FOUND */
	Station getById(Long stationId);

	/** 이름·주소에 검색어가 들어간 충전소. null인 조건은 무시한다 */
	List<Station> search(String keyword, String metro, String city, int limit);

	List<Station> findAllInBox(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng);

}
