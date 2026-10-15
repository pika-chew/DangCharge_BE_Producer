package com.picka_chew.dangcharge.domain.station.adaptor;

import java.util.Collection;
import java.util.List;

import com.picka_chew.dangcharge.domain.station.entity.Charger;

public interface ChargerAdaptor {

	/**
	 * 비관적 락(SELECT ... FOR UPDATE)으로 조회한다. 예약 생성을 충전기 단위로 직렬화해
	 * 겹침 확인과 저장 사이에 다른 요청이 끼어들지 못하게 한다. 없으면 CHARGER_NOT_FOUND
	 */
	Charger getByIdForUpdate(Long chargerId);

	List<Charger> findAllByStationId(Long stationId);

	List<Charger> findAllByStationIds(Collection<Long> stationIds);

}
