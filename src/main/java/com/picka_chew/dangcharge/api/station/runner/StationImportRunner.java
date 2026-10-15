package com.picka_chew.dangcharge.api.station.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.station.service.SyncStationsUseCase;
import com.picka_chew.dangcharge.domain.station.model.StationSyncResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * dangcharge.station-import.enabled=true로 띄우면 시작할 때 충전소 설치현황을 한 번 적재한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dangcharge.station-import.enabled", havingValue = "true")
public class StationImportRunner implements ApplicationRunner {

	private final SyncStationsUseCase syncStationsUseCase;

	@Override
	public void run(ApplicationArguments args) {
		StationSyncResult result = syncStationsUseCase.execute();
		log.info("충전소 적재 완료: 신규 {}건, 갱신 {}건", result.created(), result.updated());
	}

}
