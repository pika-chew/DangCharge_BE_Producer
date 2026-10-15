package com.picka_chew.dangcharge.api.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.api.station.dto.ChargerResponse;
import com.picka_chew.dangcharge.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class GetStationUseCaseTest {

	@Mock
	StationAdaptor stationAdaptor;
	@Mock
	ChargerAdaptor chargerAdaptor;
	@InjectMocks
	GetStationUseCase useCase;

	@Test
	void includesAllChargersOfStation() {
		Station station = Station.builder().id(1L).name("강남 충전소").rapidCount(1).slowCount(1)
				.supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build();
		given(stationAdaptor.getById(1L)).willReturn(station);
		given(chargerAdaptor.findAllByStationId(1L)).willReturn(List.of(
				Charger.builder().id(10L).station(station).chargingType(ChargingType.FAST)
						.operationalStatus(OperationalStatus.AVAILABLE).build(),
				Charger.builder().id(11L).station(station).chargingType(ChargingType.SLOW)
						.operationalStatus(OperationalStatus.OUT_OF_SERVICE).build()));

		StationDetailResponse response = useCase.execute(1L);

		assertThat(response.supportedModels()).containsExactly(VehicleModel.TESLA);
		assertThat(response.chargers()).extracting(ChargerResponse::chargerId).containsExactly(10L, 11L);
		assertThat(response.chargers().get(1).status()).isEqualTo(OperationalStatus.OUT_OF_SERVICE);
	}

}
