package com.picka_chew.dangcharge.api.station.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;

import lombok.RequiredArgsConstructor;

/**
 * 지도 반경 안의 충전소. 위경도 사각형으로 먼저 거른 뒤 실제 거리(하버사인)로 다시 거른다.
 * 좌표가 없는 충전소는 나오지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class GetNearbyStationsUseCase {

	static final double MAX_RADIUS_METERS = 20_000;
	private static final double EARTH_RADIUS_METERS = 6_371_000;
	private static final double METERS_PER_DEGREE = 111_320;
	private static final int MAX_RESULTS = 200;

	private final StationAdaptor stationAdaptor;
	private final ChargerAdaptor chargerAdaptor;

	@Transactional(readOnly = true)
	public List<StationMarkerResponse> execute(BigDecimal lat, BigDecimal lng, BigDecimal radius,
			String connectorType, BigDecimal minKw, boolean availableOnly) {
		double latitude = lat.doubleValue();
		double longitude = lng.doubleValue();
		double radiusMeters = radius.doubleValue();
		if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
			throw new GeneralException(ErrorStatus.BAD_REQUEST);
		}
		if (radiusMeters <= 0 || radiusMeters > MAX_RADIUS_METERS) {
			throw new GeneralException(ErrorStatus.INVALID_RADIUS);
		}

		double latDelta = radiusMeters / METERS_PER_DEGREE;
		double lngDelta = radiusMeters / (METERS_PER_DEGREE * Math.max(Math.cos(Math.toRadians(latitude)), 0.01));
		List<Station> candidates = stationAdaptor.findAllInBox(degrees(latitude - latDelta),
				degrees(latitude + latDelta), degrees(longitude - lngDelta), degrees(longitude + lngDelta));

		Map<Long, List<Charger>> chargersByStation = chargerAdaptor.findAllByStationIds(
						candidates.stream().map(Station::getId).toList()).stream()
				.collect(Collectors.groupingBy(charger -> charger.getStation().getId()));
		boolean filtersChargers = connectorType != null || minKw != null || availableOnly;

		List<StationMarkerResponse> markers = new ArrayList<>();
		for (Station station : candidates) {
			long distance = Math.round(distanceMeters(latitude, longitude, station.getLatitude().doubleValue(),
					station.getLongitude().doubleValue()));
			if (distance > radiusMeters) {
				continue;
			}
			List<Charger> chargers = chargersByStation.getOrDefault(station.getId(), List.of());
			if (filtersChargers && chargers.stream()
					.noneMatch(charger -> matches(charger, connectorType, minKw, availableOnly))) {
				continue;
			}
			int available = (int) chargers.stream()
					.filter(charger -> charger.getOperationalStatus() == OperationalStatus.AVAILABLE).count();
			markers.add(StationMarkerResponse.of(station, available, distance));
		}
		markers.sort(Comparator.comparingLong(StationMarkerResponse::distanceMeters));
		return markers.size() > MAX_RESULTS ? markers.subList(0, MAX_RESULTS) : markers;
	}

	private static boolean matches(Charger charger, String connectorType, BigDecimal minKw, boolean availableOnly) {
		if (connectorType != null && !connectorType.equalsIgnoreCase(charger.getConnectorType())) {
			return false;
		}
		if (minKw != null && (charger.getMaxPowerKw() == null || charger.getMaxPowerKw().compareTo(minKw) < 0)) {
			return false;
		}
		return !availableOnly || charger.getOperationalStatus() == OperationalStatus.AVAILABLE;
	}

	static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
		double dLat = Math.toRadians(lat2 - lat1);
		double dLng = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
		return 2 * EARTH_RADIUS_METERS * Math.asin(Math.min(1, Math.sqrt(a)));
	}

	private static BigDecimal degrees(double value) {
		return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
	}

}
