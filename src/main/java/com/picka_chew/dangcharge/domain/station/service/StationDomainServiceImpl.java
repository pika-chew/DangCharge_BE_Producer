package com.picka_chew.dangcharge.domain.station.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.common.annotation.DomainService;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;

import lombok.RequiredArgsConstructor;

@Transactional
@DomainService
@RequiredArgsConstructor
public class StationDomainServiceImpl implements StationDomainService {

	private final StationRepository stationRepository;

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
		int updated = 0;
		for (Map.Entry<StationKey, StationSnapshot> entry : merged.entrySet()) {
			StationSnapshot snapshot = entry.getValue();
			Station station = existing.get(entry.getKey());
			if (station != null) {
				station.sync(snapshot.metro(), snapshot.city(), snapshot.rapidCount(), snapshot.slowCount(),
						snapshot.supportedModels(), syncedAt);
				updated++;
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
		return new StationSyncResult(created.size(), updated);
	}

	/** 설치현황에 충전소 ID가 없어 이름과 주소로 같은 충전소인지 구분한다 */
	private record StationKey(String name, String address) {

		static StationKey of(String name, String address) {
			return new StationKey(name.trim(), address.trim());
		}

	}

}
