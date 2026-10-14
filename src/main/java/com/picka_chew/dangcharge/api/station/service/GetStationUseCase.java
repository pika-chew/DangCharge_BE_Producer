package com.picka_chew.dangcharge.api.station.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;

import lombok.RequiredArgsConstructor;

/** 충전소 정보와 소속 충전기 전체를 함께 내려준다 */
@UseCase
@RequiredArgsConstructor
public class GetStationUseCase {

	private final StationAdaptor stationAdaptor;
	private final ChargerAdaptor chargerAdaptor;

	@Transactional(readOnly = true)
	public StationDetailResponse execute(Long stationId) {
		return StationDetailResponse.of(stationAdaptor.getById(stationId), chargerAdaptor.findAllByStationId(stationId));
	}

}
