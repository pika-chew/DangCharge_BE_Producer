package com.picka_chew.dangcharge.api.station.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetStationUseCase {

	private final StationAdaptor stationAdaptor;

	@Transactional(readOnly = true)
	public StationDetailResponse execute(Long stationId) {
		return StationDetailResponse.from(stationAdaptor.getById(stationId));
	}

}
