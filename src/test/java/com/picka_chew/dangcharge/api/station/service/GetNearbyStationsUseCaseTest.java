package com.picka_chew.dangcharge.api.station.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

@ExtendWith(MockitoExtension.class)
class GetNearbyStationsUseCaseTest {

	private static final BigDecimal LAT = new BigDecimal("37.5000000");
	private static final BigDecimal LNG = new BigDecimal("127.0000000");

	@Mock
	StationAdaptor stationAdaptor;
	@Mock
	ChargerAdaptor chargerAdaptor;
	@InjectMocks
	GetNearbyStationsUseCase useCase;

	@Test
	void keepsStationsInsideRadiusSortedByDistance() {
		Station far = station(1L, "37.5080000");
		Station near = station(2L, "37.5010000");
		Station outside = station(3L, "37.5100000");
		given(stationAdaptor.findAllInBox(any(), any(), any(), any())).willReturn(List.of(far, near, outside));
		given(chargerAdaptor.findAllByStationIds(anyList())).willReturn(List.of(
				Charger.builder().id(10L).station(near).operationalStatus(OperationalStatus.AVAILABLE).build()));

		List<StationMarkerResponse> markers = useCase.execute(LAT, LNG, new BigDecimal("1000"), null, false);

		assertThat(markers).extracting(StationMarkerResponse::stationId).containsExactly(2L, 1L);
		assertThat(markers.get(0).availableChargerCount()).isEqualTo(1);
		assertThat(markers.get(0).distanceMeters()).isBetween(100L, 120L);
	}

	@Test
	void availableOnlyDropsStationsWithoutAvailableCharger() {
		Station withAvailable = station(1L, "37.5010000");
		Station busy = station(2L, "37.5020000");
		given(stationAdaptor.findAllInBox(any(), any(), any(), any())).willReturn(List.of(withAvailable, busy));
		given(chargerAdaptor.findAllByStationIds(anyList())).willReturn(List.of(
				Charger.builder().id(10L).station(withAvailable).operationalStatus(OperationalStatus.AVAILABLE).build(),
				Charger.builder().id(11L).station(busy).operationalStatus(OperationalStatus.IN_USE).build()));

		List<StationMarkerResponse> markers = useCase.execute(LAT, LNG, new BigDecimal("1000"), null, true);

		assertThat(markers).extracting(StationMarkerResponse::stationId).containsExactly(1L);
	}

	@Test
	void rejectsRadiusOutOfRange() {
		assertThatThrownBy(() -> useCase.execute(LAT, LNG, new BigDecimal("20001"), null, false))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RADIUS);
		assertThatThrownBy(() -> useCase.execute(LAT, LNG, BigDecimal.ZERO, null, false))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_RADIUS);
	}

	private static Station station(Long id, String latitude) {
		return Station.builder().id(id).name("충전소" + id).latitude(new BigDecimal(latitude)).longitude(LNG).build();
	}

}
