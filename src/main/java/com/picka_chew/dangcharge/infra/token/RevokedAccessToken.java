package com.picka_chew.dangcharge.infra.token;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 로그아웃으로 막은 액세스 토큰. 토큰이 원래 만료되는 시각이 지나면 지운다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "revoked_access_tokens", indexes = @Index(name = "idx_revoked_access_tokens_expires", columnList = "expires_at"))
public class RevokedAccessToken {

	/** 액세스 토큰 jti */
	@Id
	@Column(name = "token_id", length = 36)
	private String tokenId;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	RevokedAccessToken(String tokenId, Instant expiresAt) {
		this.tokenId = tokenId;
		this.expiresAt = expiresAt;
	}

}
