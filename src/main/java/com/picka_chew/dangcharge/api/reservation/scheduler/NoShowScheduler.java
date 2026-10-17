package com.picka_chew.dangcharge.api.reservation.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.reservation.service.MarkNoShowReservationsUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 1분마다 노쇼 예약을 정리한다. 대상 행을 잠그며 가져와서 레플리카마다 돌아도 같은 예약을 두 번 처리하지 않는다 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dangcharge.no-show.enabled", havingValue = "true", matchIfMissing = true)
public class NoShowScheduler {

	private final MarkNoShowReservationsUseCase markNoShowReservationsUseCase;

	@Scheduled(fixedDelayString = "${dangcharge.no-show.interval:60000}")
	public void markNoShows() {
		try {
			int count = markNoShowReservationsUseCase.execute();
			if (count > 0) {
				log.info("노쇼 처리: {}건", count);
			}
		}
		catch (RuntimeException exception) {
			log.error("노쇼 처리 실패", exception);
		}
	}

}
