package com.picka_chew.dangcharge.security.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 설정. RS256으로 서명하므로 producer는 개인키로 발급하고, consumer는 공개키만으로 검증할 수 있다.
 *
 * @param privateKey PKCS#8 RSA 개인키(PEM 또는 Base64). 비어 있으면 기동할 때마다 임시 키를 만든다
 * @param publicKey X.509 RSA 공개키(PEM 또는 Base64)
 * @param issuer 설정하지 않으면 dangcharge
 * @param accessTokenTtl 설정하지 않으면 30분
 * @param refreshTokenTtl 설정하지 않으면 14일
 */
@ConfigurationProperties("dangcharge.jwt")
public record JwtProperties(
		String issuer,
		String privateKey,
		String publicKey,
		Duration accessTokenTtl,
		Duration refreshTokenTtl) {

	public JwtProperties {
		issuer = issuer == null || issuer.isBlank() ? "dangcharge" : issuer;
		accessTokenTtl = accessTokenTtl == null ? Duration.ofMinutes(30) : accessTokenTtl;
		refreshTokenTtl = refreshTokenTtl == null ? Duration.ofDays(14) : refreshTokenTtl;
	}

	public boolean hasKeyPair() {
		return privateKey != null && !privateKey.isBlank() && publicKey != null && !publicKey.isBlank();
	}

}
