package com.picka_chew.dangcharge.api.station.service;

import java.util.List;

import com.picka_chew.dangcharge.api.station.port.StationDataReader;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.service.StationDomainService;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class ImportStationsUseCase {

	private final StationDataReader stationDataReader;
	private final StationDomainService stationDomainService;

	public StationSyncResult execute() {
		List<StationSnapshot> snapshots = stationDataReader.read();
		return stationDomainService.syncAll(snapshots);
	}

}
