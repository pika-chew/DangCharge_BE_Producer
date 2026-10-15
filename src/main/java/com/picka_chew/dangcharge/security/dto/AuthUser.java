package com.picka_chew.dangcharge.security.dto;

import java.time.Instant;

/**
 * 요청을 보낸 로그인 사용자.
 *
 * @param tokenId 액세스 토큰 jti. 로그아웃할 때 막을 토큰을 가리킨다
 * @param tokenExpiresAt 액세스 토큰 만료 시각
 */
public record AuthUser(Long userId, String tokenId, Instant tokenExpiresAt) {
}
