package com.picka_chew.dangcharge.api.station.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.port.StationDataReader;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.event.StationUpsertedEvent;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.service.StationDomainService;

import lombok.RequiredArgsConstructor;

/**
 * 외부 설치현황을 DB에 반영하고, 새로 생기거나 값·가상 충전기가 바뀐 충전소만 이벤트로 남긴다.
 * 관리 API, 스케줄러, 기동 시 적재가 모두 이 UseCase를 쓴다.
 */
@UseCase
@RequiredArgsConstructor
public class SyncStationsUseCase {

	private final StationDataReader stationDataReader;
	private final StationDomainService stationDomainService;
	private final ChargerAdaptor chargerAdaptor;
	private final DomainEventRecorder eventRecorder;

	@Transactional
	public StationSyncResult execute() {
		List<StationSnapshot> snapshots = stationDataReader.read();
		StationSyncResult result = stationDomainService.syncAll(snapshots);
		Map<Long, List<Charger>> chargersByStation = chargerAdaptor.findAllByStationIds(
						result.changedStations().stream().map(Station::getId).toList()).stream()
				.collect(Collectors.groupingBy(charger -> charger.getStation().getId()));
		for (Station station : result.changedStations()) {
			eventRecorder.record(StationUpsertedEvent.of(station,
					chargersByStation.getOrDefault(station.getId(), List.of())));
		}
		return result;
	}

}
