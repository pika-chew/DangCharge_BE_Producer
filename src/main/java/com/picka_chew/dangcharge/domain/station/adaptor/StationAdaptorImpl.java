package com.picka_chew.dangcharge.domain.station.adaptor;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.picka_chew.dangcharge.common.annotation.Adaptor;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
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

	@Override
	public Station getById(Long stationId) {
		return stationRepository.findWithSupportedModelsById(stationId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.STATION_NOT_FOUND));
	}

	@Override
	public List<Station> search(String keyword, String metro, String city, int limit) {
		return stationRepository.search(keyword, metro, city, PageRequest.of(0, limit));
	}

	@Override
	public List<Station> findAllInBox(BigDecimal minLat, BigDecimal maxLat, BigDecimal minLng, BigDecimal maxLng) {
		return stationRepository.findAllInBox(minLat, maxLat, minLng, maxLng);
	}

}
