package com.picka_chew.dangcharge.api.station.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.ChargerResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetChargersUseCase {

	private final StationAdaptor stationAdaptor;
	private final ChargerAdaptor chargerAdaptor;

	@Transactional(readOnly = true)
	public List<ChargerResponse> execute(Long stationId) {
		stationAdaptor.getById(stationId);
		return chargerAdaptor.findAllByStationId(stationId).stream().map(ChargerResponse::from).toList();
	}

}
