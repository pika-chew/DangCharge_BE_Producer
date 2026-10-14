package com.picka_chew.dangcharge.domain.station.service;

import java.util.Collection;

import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;

public interface StationDomainService {

	/**
	 * 외부 설치현황을 충전소에 반영한다. 이름과 주소가 같으면 같은 충전소로 보고 갱신하고, 없으면 새로 만든다.
	 * 설치현황에서 빠진 충전소는 지우지 않는다. 트랜잭션은 호출하는 쪽이 연다.
	 */
	StationSyncResult syncAll(Collection<StationSnapshot> snapshots);

}
