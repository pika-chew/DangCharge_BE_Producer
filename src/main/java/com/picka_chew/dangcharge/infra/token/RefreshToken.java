package com.picka_chew.dangcharge.infra.token;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자별 리프레시 토큰. 다시 로그인하면 덮어써서 이전 토큰은 무효가 된다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken {

	@Id
	@Column(name = "user_id")
	private Long userId;

	/** 토큰의 SHA-256 해시(hex). DB가 유출돼도 토큰을 그대로 쓸 수 없게 원문은 저장하지 않는다 */
	@Column(name = "token_hash", nullable = false, length = 64)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

}
