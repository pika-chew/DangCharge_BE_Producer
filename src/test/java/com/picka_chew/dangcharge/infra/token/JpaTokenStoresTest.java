package com.picka_chew.dangcharge.infra.token;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import com.picka_chew.dangcharge.common.config.TimeConfig;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;
import com.picka_chew.dangcharge.infra.idempotency.JpaIdempotencyStore;

/**
 * 토큰·Idempotency-Key 저장소가 실제 MySQL에서 동작하는지 확인한다. 리프레시 토큰 저장은 MySQL 전용 SQL을 쓴다.
 * 테스트마다 롤백하므로 데이터가 남지 않는다.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaRefreshTokenStore.class, JpaAccessTokenBlacklist.class, JpaIdempotencyStore.class, TimeConfig.class})
class JpaTokenStoresTest {

	@Autowired
	JpaRefreshTokenStore refreshTokenStore;
	@Autowired
	JpaAccessTokenBlacklist accessTokenBlacklist;
	@Autowired
	JpaIdempotencyStore idempotencyStore;
	@Autowired
	UserRepository userRepository;

	Long userId;

	@BeforeEach
	void createUser() {
		userId = userRepository.saveAndFlush(User.builder()
				.loginId("store-test-" + System.nanoTime()).passwordHash("hash").username("테스트")
				.vehicleModel(VehicleModel.TESLA).build()).getId();
	}

	@Test
	void latestRefreshTokenReplacesPreviousOne() {
		refreshTokenStore.save(userId, "first", Duration.ofDays(14));
		refreshTokenStore.save(userId, "second", Duration.ofDays(14));

		assertThat(refreshTokenStore.matches(userId, "second")).isTrue();
		assertThat(refreshTokenStore.matches(userId, "first")).isFalse();

		refreshTokenStore.delete(userId);
		assertThat(refreshTokenStore.matches(userId, "second")).isFalse();
	}

	@Test
	void expiredRefreshTokenDoesNotMatch() {
		refreshTokenStore.save(userId, "token", Duration.ofSeconds(-1));

		assertThat(refreshTokenStore.matches(userId, "token")).isFalse();
	}

	@Test
	void revokedAccessTokenIsBlacklisted() {
		accessTokenBlacklist.add("jti-1", Duration.ofMinutes(10));

		assertThat(accessTokenBlacklist.contains("jti-1")).isTrue();
		assertThat(accessTokenBlacklist.contains("jti-2")).isFalse();
	}

	@Test
	void idempotencyKeyIsScopedToUser() {
		idempotencyStore.save(userId, "key", 55L);

		assertThat(idempotencyStore.findReservationId(userId, "key")).contains(55L);
		assertThat(idempotencyStore.findReservationId(userId + 1, "key")).isEmpty();
	}

}
