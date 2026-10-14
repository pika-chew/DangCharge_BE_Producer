package com.picka_chew.dangcharge.domain.station.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.common.annotation.DomainService;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargerDataSource;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.OperationalStatus;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;

import lombok.RequiredArgsConstructor;

@Transactional
@DomainService
@RequiredArgsConstructor
public class StationDomainServiceImpl implements StationDomainService {

	private final StationRepository stationRepository;
	private final ChargerRepository chargerRepository;

	@Override
	public StationSyncResult syncAll(Collection<StationSnapshot> snapshots) {
		Map<StationKey, StationSnapshot> merged = new LinkedHashMap<>();
		for (StationSnapshot snapshot : snapshots) {
			merged.merge(StationKey.of(snapshot.name(), snapshot.address()), snapshot, StationSnapshot::merge);
		}

		Map<StationKey, Station> existing = new HashMap<>();
		for (Station station : stationRepository.findAllWithSupportedModels()) {
			existing.put(StationKey.of(station.getName(), station.getAddress()), station);
		}

		Instant syncedAt = Instant.now();
		List<Station> created = new ArrayList<>();
		List<Station> updated = new ArrayList<>();
		for (Map.Entry<StationKey, StationSnapshot> entry : merged.entrySet()) {
			StationSnapshot snapshot = entry.getValue();
			Station station = existing.get(entry.getKey());
			if (station != null) {
				if (station.sync(snapshot.metro(), snapshot.city(), snapshot.rapidCount(), snapshot.slowCount(),
						snapshot.supportedModels(), syncedAt)) {
					updated.add(station);
				}
				continue;
			}
			created.add(Station.builder()
					.name(snapshot.name())
					.address(snapshot.address())
					.metro(snapshot.metro())
					.city(snapshot.city())
					.rapidCount(snapshot.rapidCount())
					.slowCount(snapshot.slowCount())
					.supportedModels(new HashSet<>(snapshot.supportedModels()))
					.syncedAt(syncedAt)
					.build());
		}
		stationRepository.saveAll(created);

		List<Station> synced = new ArrayList<>(created);
		for (StationKey key : merged.keySet()) {
			Station station = existing.get(key);
			if (station != null) {
				synced.add(station);
			}
		}
		Set<Station> changed = new LinkedHashSet<>(created);
		changed.addAll(updated);
		changed.addAll(syncSimulatedChargers(synced));
		return new StationSyncResult(created.size(), updated.size(), List.copyOf(changed));
	}

	/**
	 * 설치현황에는 충전기 개별 정보가 없어 급속·완속 수만큼 가상 충전기(SIMULATION)를 맞춘다.
	 * 모자라면 만들고, 남으면 지우지 않고 OUT_OF_SERVICE로 돌린다. 예약이 걸린 충전기를 지울 수 없기 때문이다.
	 * 실제 충전기(REAL)는 건드리지 않는다.
	 *
	 * @return 가상 충전기가 바뀐 충전소
	 */
	private List<Station> syncSimulatedChargers(List<Station> stations) {
		Map<Long, List<Charger>> simulatedByStation = chargerRepository.findAllByDataSource(ChargerDataSource.SIMULATION)
				.stream()
				.collect(Collectors.groupingBy(charger -> charger.getStation().getId()));
		List<Charger> newChargers = new ArrayList<>();
		List<Station> changed = new ArrayList<>();
		for (Station station : stations) {
			List<Charger> current = simulatedByStation.getOrDefault(station.getId(), List.of());
			boolean fast = adjust(station, ChargingType.FAST, station.getRapidCount(), current, newChargers);
			boolean slow = adjust(station, ChargingType.SLOW, station.getSlowCount(), current, newChargers);
			if (fast || slow) {
				changed.add(station);
			}
		}
		chargerRepository.saveAll(newChargers);
		return changed;
	}

	private static boolean adjust(Station station, ChargingType type, int target, List<Charger> current,
			List<Charger> newChargers) {
		List<Charger> ofType = current.stream()
				.filter(charger -> charger.getChargingType() == type)
				.sorted(Comparator.comparing(Charger::getId))
				.toList();
		boolean changed = false;
		for (int i = 0; i < ofType.size(); i++) {
			Charger charger = ofType.get(i);
			if (i < target && charger.getOperationalStatus() == OperationalStatus.OUT_OF_SERVICE) {
				charger.changeOperationalStatus(OperationalStatus.AVAILABLE);
				changed = true;
			}
			else if (i >= target && charger.getOperationalStatus() != OperationalStatus.OUT_OF_SERVICE) {
				charger.changeOperationalStatus(OperationalStatus.OUT_OF_SERVICE);
				changed = true;
			}
		}
		for (int i = ofType.size(); i < target; i++) {
			newChargers.add(Charger.simulated(station, type));
			changed = true;
		}
		return changed;
	}

	/** 설치현황에 충전소 ID가 없어 이름과 주소로 같은 충전소인지 구분한다 */
	private record StationKey(String name, String address) {

		static StationKey of(String name, String address) {
			return new StationKey(name.trim(), address.trim());
		}

	}

}
