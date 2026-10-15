package com.picka_chew.dangcharge.infra.token;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	/** 같은 사용자가 동시에 로그인해도 충돌하지 않게 한 문장으로 넣거나 덮어쓴다 */
	@Modifying
	@NativeQuery("""
			insert into refresh_tokens (user_id, token_hash, expires_at) values (:userId, :tokenHash, :expiresAt)
			on duplicate key update token_hash = values(token_hash), expires_at = values(expires_at)
			""")
	void upsert(@Param("userId") Long userId, @Param("tokenHash") String tokenHash,
			@Param("expiresAt") Instant expiresAt);

}
