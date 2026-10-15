package com.picka_chew.dangcharge.api.station.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.api.station.port.StationDataReader;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.event.StationUpsertedEvent;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.service.StationDomainService;

@ExtendWith(MockitoExtension.class)
class SyncStationsUseCaseTest {

	@Mock
	StationDataReader stationDataReader;
	@Mock
	StationDomainService stationDomainService;
	@Mock
	ChargerAdaptor chargerAdaptor;
	@Mock
	DomainEventRecorder eventRecorder;
	@InjectMocks
	SyncStationsUseCase useCase;

	@Test
	void recordsEventOnlyForChangedStations() {
		Station created = Station.builder().id(1L).name("신규").supportedModels(new HashSet<>()).build();
		Station updated = Station.builder().id(2L).name("변경").supportedModels(new HashSet<>()).build();
		given(stationDataReader.read()).willReturn(List.of());
		given(stationDomainService.syncAll(any())).willReturn(new StationSyncResult(1, 1, List.of(created, updated)));

		useCase.execute();

		verify(eventRecorder, times(2)).record(any(StationUpsertedEvent.class));
		verify(eventRecorder).record(argThat(event -> event.aggregateId().equals(1L)));
		verify(eventRecorder).record(argThat(event -> event.aggregateId().equals(2L)));
	}

}
