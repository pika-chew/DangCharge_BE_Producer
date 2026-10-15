package com.picka_chew.dangcharge.domain.station.adaptor;

import java.util.Collection;
import java.util.List;

import com.picka_chew.dangcharge.common.annotation.Adaptor;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class ChargerAdaptorImpl implements ChargerAdaptor {

	private final ChargerRepository chargerRepository;

	@Override
	public Charger getByIdForUpdate(Long chargerId) {
		return chargerRepository.findByIdForUpdate(chargerId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.CHARGER_NOT_FOUND));
	}

	@Override
	public List<Charger> findAllByStationId(Long stationId) {
		return chargerRepository.findAllByStationIdOrderById(stationId);
	}

	@Override
	public List<Charger> findAllByStationIds(Collection<Long> stationIds) {
		if (stationIds.isEmpty()) {
			return List.of();
		}
		return chargerRepository.findAllByStationIdIn(stationIds);
	}

}
