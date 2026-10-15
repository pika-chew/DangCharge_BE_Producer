package com.picka_chew.dangcharge.security.config;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.picka_chew.dangcharge.api.auth.port.AccessTokenBlacklist;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

	public static final String TOKEN_TYPE_CLAIM = "token_type";
	public static final String ACCESS = "access";
	public static final String REFRESH = "refresh";

	@Bean
	public KeyPair jwtKeyPair(JwtProperties properties) throws Exception {
		if (properties.hasKeyPair()) {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			return new KeyPair(
					keyFactory.generatePublic(new X509EncodedKeySpec(decodePem(properties.publicKey()))),
					keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decodePem(properties.privateKey()))));
		}
		log.warn("JWT 키가 설정되지 않아 임시 키를 만듭니다. 재시작하면 발급한 토큰이 모두 무효가 되니 운영에서는 "
				+ "JWT_PRIVATE_KEY, JWT_PUBLIC_KEY를 설정하세요.");
		return generateKeyPair();
	}

	@Bean
	public JwtEncoder jwtEncoder(KeyPair jwtKeyPair) {
		RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) jwtKeyPair.getPublic())
				.privateKey((RSAPrivateKey) jwtKeyPair.getPrivate())
				.build();
		return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
	}

	/** 요청 인증용. 액세스 토큰이고 로그아웃으로 막힌 토큰이 아니어야 한다 */
	@Bean
	@Primary
	public JwtDecoder accessTokenDecoder(KeyPair jwtKeyPair, JwtProperties properties,
			AccessTokenBlacklist blacklist) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()),
				tokenType(ACCESS),
				jwt -> jwt.getId() != null && blacklist.contains(jwt.getId())
						? OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "로그아웃한 토큰입니다", null))
						: OAuth2TokenValidatorResult.success()));
		return decoder;
	}

	@Bean
	public JwtDecoder refreshTokenDecoder(KeyPair jwtKeyPair, JwtProperties properties) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) jwtKeyPair.getPublic()).build();
		decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
				JwtValidators.createDefaultWithIssuer(properties.issuer()), tokenType(REFRESH)));
		return decoder;
	}

	private static OAuth2TokenValidator<Jwt> tokenType(String expected) {
		return jwt -> expected.equals(jwt.getClaimAsString(TOKEN_TYPE_CLAIM))
				? OAuth2TokenValidatorResult.success()
				: OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "토큰 종류가 다릅니다", null));
	}

	private static byte[] decodePem(String value) {
		String body = value.replaceAll("-----(BEGIN|END) [A-Z ]+-----", "").replaceAll("\\s", "");
		return Base64.getDecoder().decode(body);
	}

	private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		return generator.generateKeyPair();
	}

}
