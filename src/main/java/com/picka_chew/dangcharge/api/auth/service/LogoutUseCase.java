package com.picka_chew.dangcharge.api.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import com.picka_chew.dangcharge.api.auth.port.AccessTokenBlacklist;
import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import lombok.RequiredArgsConstructor;

/** 리프레시 토큰을 지우고, 지금 쓰던 액세스 토큰은 남은 만료 시간 동안 막는다 */
@UseCase
@RequiredArgsConstructor
public class LogoutUseCase {

	private final RefreshTokenStore refreshTokenStore;
	private final AccessTokenBlacklist accessTokenBlacklist;
	private final Clock clock;

	public void execute(AuthUser authUser) {
		refreshTokenStore.delete(authUser.userId());
		if (authUser.tokenId() != null && authUser.tokenExpiresAt() != null) {
			accessTokenBlacklist.add(authUser.tokenId(),
					Duration.between(Instant.now(clock), authUser.tokenExpiresAt()));
		}
	}

}
