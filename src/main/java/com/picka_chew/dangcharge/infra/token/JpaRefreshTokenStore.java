package com.picka_chew.dangcharge.infra.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JpaRefreshTokenStore implements RefreshTokenStore {

	private final RefreshTokenRepository refreshTokenRepository;
	private final Clock clock;

	@Override
	@Transactional
	public void save(Long userId, String refreshToken, Duration ttl) {
		refreshTokenRepository.upsert(userId, TokenHasher.sha256(refreshToken), Instant.now(clock).plus(ttl));
	}

	@Override
	@Transactional(readOnly = true)
	public boolean matches(Long userId, String refreshToken) {
		return refreshTokenRepository.findById(userId)
				.filter(stored -> stored.getExpiresAt().isAfter(Instant.now(clock)))
				.map(stored -> MessageDigest.isEqual(
						stored.getTokenHash().getBytes(StandardCharsets.US_ASCII),
						TokenHasher.sha256(refreshToken).getBytes(StandardCharsets.US_ASCII)))
				.orElse(false);
	}

	@Override
	@Transactional
	public void delete(Long userId) {
		refreshTokenRepository.deleteById(userId);
	}

}
