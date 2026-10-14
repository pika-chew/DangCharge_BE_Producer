package com.picka_chew.dangcharge.security.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.auth.port.TokenIssuer;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.security.config.JwtConfig;
import com.picka_chew.dangcharge.security.config.JwtProperties;

@Component
public class JwtTokenIssuer implements TokenIssuer {

	private final JwtEncoder jwtEncoder;
	private final JwtDecoder refreshTokenDecoder;
	private final JwtProperties properties;
	private final Clock clock;

	public JwtTokenIssuer(JwtEncoder jwtEncoder, @Qualifier("refreshTokenDecoder") JwtDecoder refreshTokenDecoder,
			JwtProperties properties, Clock clock) {
		this.jwtEncoder = jwtEncoder;
		this.refreshTokenDecoder = refreshTokenDecoder;
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	public String issueAccessToken(Long userId) {
		return issue(userId, JwtConfig.ACCESS, properties.accessTokenTtl());
	}

	@Override
	public String issueRefreshToken(Long userId) {
		return issue(userId, JwtConfig.REFRESH, properties.refreshTokenTtl());
	}

	@Override
	public Long verifyRefreshToken(String refreshToken) {
		try {
			return Long.valueOf(refreshTokenDecoder.decode(refreshToken).getSubject());
		}
		catch (JwtException | NumberFormatException exception) {
			throw new GeneralException(ErrorStatus.INVALID_REFRESH_TOKEN);
		}
	}

	@Override
	public Duration refreshTokenTtl() {
		return properties.refreshTokenTtl();
	}

	private String issue(Long userId, String tokenType, Duration ttl) {
		Instant now = Instant.now(clock);
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.subject(String.valueOf(userId))
				.id(UUID.randomUUID().toString())
				.issuedAt(now)
				.expiresAt(now.plus(ttl))
				.claim(JwtConfig.TOKEN_TYPE_CLAIM, tokenType)
				.build();
		JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}

}
