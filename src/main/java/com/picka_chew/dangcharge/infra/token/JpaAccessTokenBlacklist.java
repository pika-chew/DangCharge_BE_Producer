package com.picka_chew.dangcharge.infra.token;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.auth.port.AccessTokenBlacklist;

import lombok.RequiredArgsConstructor;

/** 인증이 필요한 요청마다 PK 조회가 한 번 일어난다 */
@Component
@RequiredArgsConstructor
public class JpaAccessTokenBlacklist implements AccessTokenBlacklist {

	private final RevokedAccessTokenRepository revokedAccessTokenRepository;
	private final Clock clock;

	@Override
	@Transactional
	public void add(String tokenId, Duration ttl) {
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}
		revokedAccessTokenRepository.save(new RevokedAccessToken(tokenId, Instant.now(clock).plus(ttl)));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean contains(String tokenId) {
		return revokedAccessTokenRepository.existsById(tokenId);
	}

}
