package com.picka_chew.dangcharge.api.station.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.station.service.SyncStationsUseCase;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 충전소 설치현황을 주기적으로 동기화한다. 레플리카마다 돌기 때문에 기본은 꺼 두고,
 * 한 곳에서만 dangcharge.station-sync.enabled=true로 켠다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dangcharge.station-sync.enabled", havingValue = "true")
public class StationSyncScheduler {

	private final SyncStationsUseCase syncStationsUseCase;

	@Scheduled(cron = "${dangcharge.station-sync.cron:0 0 4 * * *}", zone = "Asia/Seoul")
	public void sync() {
		try {
			StationSyncResult result = syncStationsUseCase.execute();
			log.info("충전소 정기 동기화 완료: 신규 {}건, 변경 {}건", result.created(), result.updated());
		}
		catch (RuntimeException exception) {
			log.error("충전소 정기 동기화 실패", exception);
		}
	}

}
