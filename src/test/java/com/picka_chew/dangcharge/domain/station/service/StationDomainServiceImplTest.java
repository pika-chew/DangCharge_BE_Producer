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

import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargerDataSource;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class StationDomainServiceImplTest {

	@Mock
	StationRepository stationRepository;
	@Mock
	ChargerRepository chargerRepository;
	@InjectMocks
	StationDomainServiceImpl stationDomainService;
	@Captor
	ArgumentCaptor<List<Station>> savedStations;
	@Captor
	ArgumentCaptor<List<Charger>> savedChargers;

	@Test
	void createsStationsThatDoNotExistYet() {
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of());

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 2, 0, VehicleModel.TESLA)));

		assertThat(result.created()).isEqualTo(1);
		assertThat(result.updated()).isZero();
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

		assertThat(result.created()).isEqualTo(1);
		assertThat(result.updated()).isZero();
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

		assertThat(result.created()).isZero();
		assertThat(result.updated()).isEqualTo(1);
		assertThat(result.changedStations()).containsExactly(existing);
		assertThat(existing.getRapidCount()).isEqualTo(3);
		assertThat(existing.getSlowCount()).isEqualTo(1);
		assertThat(existing.getSupportedModels()).containsExactly(VehicleModel.TESLA);
		verify(stationRepository).saveAll(savedStations.capture());
		assertThat(savedStations.getValue()).isEmpty();
	}

	@Test
	void doesNotCountStationWhoseValuesAndChargersAreUnchanged() {
		Station existing = Station.builder().id(1L)
				.name("곡성지사").address("전라남도 곡성군 1").metro("전라남도").city("곡성군")
				.rapidCount(3).slowCount(1)
				.supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA)))
				.build();
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of(existing));
		given(chargerRepository.findAllByDataSource(ChargerDataSource.SIMULATION)).willReturn(List.of(
				simulated(10L, existing, ChargingType.FAST, OperationalStatus.AVAILABLE),
				simulated(11L, existing, ChargingType.FAST, OperationalStatus.IN_USE),
				simulated(12L, existing, ChargingType.FAST, OperationalStatus.AVAILABLE),
				simulated(13L, existing, ChargingType.SLOW, OperationalStatus.AVAILABLE)));

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 3, 1, VehicleModel.TESLA)));

		assertThat(result.updated()).isZero();
		assertThat(result.changedStations()).isEmpty();
		assertThat(existing.getSyncedAt()).isNotNull();
	}

	@Test
	void createsSimulatedChargersForEachRapidAndSlowCount() {
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of());

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 2, 1, VehicleModel.TESLA)));

		verify(chargerRepository).saveAll(savedChargers.capture());
		assertThat(savedChargers.getValue())
				.extracting(Charger::getChargingType)
				.containsExactly(ChargingType.FAST, ChargingType.FAST, ChargingType.SLOW);
		assertThat(savedChargers.getValue()).allSatisfy(charger -> {
			assertThat(charger.getDataSource()).isEqualTo(ChargerDataSource.SIMULATION);
			assertThat(charger.getOperationalStatus()).isEqualTo(OperationalStatus.AVAILABLE);
			assertThat(charger.getStation().getName()).isEqualTo("곡성지사");
		});
		assertThat(savedChargers.getValue().get(0).getMaxPowerKw()).isEqualByComparingTo("50");
		assertThat(savedChargers.getValue().get(2).getMaxPowerKw()).isEqualByComparingTo("7");
		assertThat(result.changedStations()).hasSize(1);
	}

	@Test
	void fewerChargersTurnsExtraSimulatedChargersOutOfServiceInsteadOfDeleting() {
		Station existing = Station.builder().id(1L)
				.name("곡성지사").address("전라남도 곡성군 1").metro("전라남도").city("곡성군")
				.rapidCount(3).slowCount(0)
				.supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA)))
				.build();
		Charger first = simulated(10L, existing, ChargingType.FAST, OperationalStatus.AVAILABLE);
		Charger second = simulated(11L, existing, ChargingType.FAST, OperationalStatus.AVAILABLE);
		Charger third = simulated(12L, existing, ChargingType.FAST, OperationalStatus.AVAILABLE);
		given(stationRepository.findAllWithSupportedModels()).willReturn(List.of(existing));
		given(chargerRepository.findAllByDataSource(ChargerDataSource.SIMULATION))
				.willReturn(List.of(third, first, second));

		StationSyncResult result = stationDomainService.syncAll(List.of(
				snapshot("곡성지사", "전라남도 곡성군 1", 1, 0, VehicleModel.TESLA)));

		assertThat(first.getOperationalStatus()).isEqualTo(OperationalStatus.AVAILABLE);
		assertThat(second.getOperationalStatus()).isEqualTo(OperationalStatus.OUT_OF_SERVICE);
		assertThat(third.getOperationalStatus()).isEqualTo(OperationalStatus.OUT_OF_SERVICE);
		verify(chargerRepository).saveAll(savedChargers.capture());
		assertThat(savedChargers.getValue()).isEmpty();
		assertThat(result.changedStations()).containsExactly(existing);
	}

	private static Charger simulated(Long id, Station station, ChargingType type, OperationalStatus status) {
		return Charger.builder().id(id).station(station).chargingType(type).operationalStatus(status)
				.dataSource(ChargerDataSource.SIMULATION).build();
	}

	private static StationSnapshot snapshot(String name, String address, int rapid, int slow, VehicleModel... models) {
		return new StationSnapshot("전라남도", "곡성군", name, address, rapid, slow, Set.of(models));
	}

}
