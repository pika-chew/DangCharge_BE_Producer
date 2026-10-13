package com.picka_chew.dangcharge.domain.station.adaptor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.common.annotation.Adaptor;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class StationAdaptorImpl implements StationAdaptor {

	private final StationRepository stationRepository;

	@Override
	public Page<Station> queryStationsSupporting(VehicleModel model, Pageable pageable) {
		return stationRepository.findAllSupporting(model, pageable);
	}

	@Override
	public Page<Station> queryStationsSupportingIn(VehicleModel model, String metro, String city, Pageable pageable) {
		return stationRepository.findAllSupportingIn(model, metro, city, pageable);
	}

}
