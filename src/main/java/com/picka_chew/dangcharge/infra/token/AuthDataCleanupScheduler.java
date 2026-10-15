package com.picka_chew.dangcharge.infra.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.infra.idempotency.ReservationIdempotencyKeyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis TTL 대신 만료된 행을 주기적으로 지운다. 여러 파드에서 함께 돌아도 결과가 같다.
 * 만료된 행이 남아 있어도 동작에는 문제가 없고 테이블만 커진다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthDataCleanupScheduler {

	static final Duration IDEMPOTENCY_KEY_RETENTION = Duration.ofHours(24);

	private final RevokedAccessTokenRepository revokedAccessTokenRepository;
	private final ReservationIdempotencyKeyRepository idempotencyKeyRepository;
	private final Clock clock;

	@Transactional
	@Scheduled(cron = "0 0 * * * *")
	public void cleanup() {
		Instant now = Instant.now(clock);
		int revoked = revokedAccessTokenRepository.deleteExpired(now);
		int keys = idempotencyKeyRepository.deleteCreatedBefore(now.minus(IDEMPOTENCY_KEY_RETENTION));
		if (revoked + keys > 0) {
			log.info("만료 데이터 정리: 로그아웃 토큰 {}건, Idempotency-Key {}건", revoked, keys);
		}
	}

}
