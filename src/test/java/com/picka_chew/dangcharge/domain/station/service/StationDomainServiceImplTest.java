package com.picka_chew.dangcharge.domain.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class StationDomainServiceImplTest {

	@Mock
	StationRepository stationRepository;
	@InjectMocks
	StationDomainServiceImpl stationDomainService;
	@Captor
	ArgumentCaptor<List<Station>> savedStations;

	@Test
	void createsStationsThatDoNotExistYet() {
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of());

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 2, 0, VehicleModel.TESLA)));

		assertThat(result).isEqualTo(new StationSyncResult(1, 0));
		verify(stationRepository).saveAll(savedStations.capture());
		Station saved = savedStations.getValue().get(0);
		assertThat(saved.getName()).isEqualTo("곡성지사");
		assertThat(saved.getRapidCount()).isEqualTo(2);
		assertThat(saved.supports(VehicleModel.TESLA)).isTrue();
		assertThat(saved.supports(VehicleModel.RAY_EV)).isFalse();
		assertThat(saved.getSyncedAt()).isNotNull();
	}

	@Test
	void mergesRowsOfTheSameStation() {
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of());

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 0, 2, VehicleModel.RAY_EV),
				snapshot("곡성지사", "전라남도 곡성군 1", 2, 0, VehicleModel.TESLA)));

		assertThat(result).isEqualTo(new StationSyncResult(1, 0));
		verify(stationRepository).saveAll(savedStations.capture());
		Station saved = savedStations.getValue().get(0);
		assertThat(saved.getRapidCount()).isEqualTo(2);
		assertThat(saved.getSlowCount()).isEqualTo(2);
		assertThat(saved.getSupportedModels()).containsExactlyInAnyOrder(VehicleModel.RAY_EV, VehicleModel.TESLA);
	}

	@Test
	void updatesStationWithSameNameAndAddress() {
		Station existing = Station.builder()
				.name("곡성지사").address("전라남도 곡성군 1").metro("전라남도").city("곡성군")
				.rapidCount(1).slowCount(0)
				.supportedModels(new HashSet<>(Set.of(VehicleModel.SM3_ZE, VehicleModel.TESLA)))
				.build();
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of(existing));

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 3, 1, VehicleModel.TESLA)));

		assertThat(result).isEqualTo(new StationSyncResult(0, 1));
		assertThat(existing.getRapidCount()).isEqualTo(3);
		assertThat(existing.getSlowCount()).isEqualTo(1);
		assertThat(existing.getSupportedModels()).containsExactly(VehicleModel.TESLA);
		verify(stationRepository).saveAll(savedStations.capture());
		assertThat(savedStations.getValue()).isEmpty();
	}

	private static StationSnapshot snapshot(String name, String address, int rapid, int slow, VehicleModel... models) {
		return new StationSnapshot("전라남도", "곡성군", name, address, rapid, slow, Set.of(models));
	}

}
