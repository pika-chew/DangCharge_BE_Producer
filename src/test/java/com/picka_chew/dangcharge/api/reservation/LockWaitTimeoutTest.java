package com.picka_chew.dangcharge.api.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import com.picka_chew.dangcharge.api.common.advice.ExceptionAdvice;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/**
 * 다른 세션이 충전기 락을 놓지 않을 때 예약 요청이 락 대기 시간(5초)만 기다리고 503으로 끝나는지 실제 MySQL로 확인한다.
 * 락을 잡는 커넥션과 예약 요청이 따로 커밋해야 해서 롤백 대신 테스트가 만든 데이터를 직접 지운다.
 */
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false",
		"dangcharge.no-show.enabled=false"})
class LockWaitTimeoutTest {

	@Autowired
	CreateReservationUseCase createReservationUseCase;
	@Autowired
	UserRepository userRepository;
	@Autowired
	StationRepository stationRepository;
	@Autowired
	ChargerRepository chargerRepository;
	@Autowired
	DataSource dataSource;
	@Autowired
	JdbcTemplate jdbcTemplate;

	Station station;
	Charger charger;
	User user;

	@BeforeEach
	void setUp() {
		String suffix = String.valueOf(System.nanoTime());
		station = stationRepository.save(Station.builder()
				.name("락대기테스트-" + suffix).address("테스트주소-" + suffix).metro("테스트").city("테스트")
				.rapidCount(1).slowCount(0).supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build());
		charger = chargerRepository.save(Charger.simulated(station, ChargingType.FAST));
		user = userRepository.save(User.builder()
				.loginId("lock-wait-" + suffix).passwordHash("hash").username("락대기")
				.vehicleModel(VehicleModel.TESLA).build());
	}

	@AfterEach
	void cleanUp() {
		jdbcTemplate.update("delete from outbox_events where aggregate_type = 'reservation' and aggregate_id in "
				+ "(select id from reservations where charger_id = ?)", charger.getId());
		jdbcTemplate.update("delete from reservations where charger_id = ?", charger.getId());
		jdbcTemplate.update("delete from chargers where id = ?", charger.getId());
		jdbcTemplate.update("delete from station_supported_models where station_id = ?", station.getId());
		jdbcTemplate.update("delete from stations where id = ?", station.getId());
		jdbcTemplate.update("delete from users where id = ?", user.getId());
	}

	@Test
	void connectionsUseShortLockWaitAndIdleTimeout() {
		Map<String, Object> timeouts = jdbcTemplate.queryForMap(
				"select @@session.innodb_lock_wait_timeout as lock_wait, @@session.wait_timeout as idle");

		assertThat(((Number) timeouts.get("lock_wait")).intValue()).isEqualTo(5);
		assertThat(((Number) timeouts.get("idle")).intValue()).isEqualTo(300);
	}

	@Test
	void reservationGivesUpAfterLockWaitTimeoutWhileChargerIsLocked() throws Exception {
		OffsetDateTime startAt = LocalDate.now(TimeUtils.ZONE).plusDays(1).atTime(10, 0)
				.atZone(TimeUtils.ZONE).toOffsetDateTime();
		CreateReservationRequest request = new CreateReservationRequest(charger.getId(), startAt);

		Throwable thrown;
		long elapsedMillis;
		try (Connection holder = dataSource.getConnection()) {
			holder.setAutoCommit(false);
			try (PreparedStatement lock = holder.prepareStatement("select id from chargers where id = ? for update")) {
				lock.setLong(1, charger.getId());
				lock.executeQuery();
			}

			long started = System.nanoTime();
			thrown = catchThrowable(() -> createReservationUseCase.execute(user.getId(), request, null));
			elapsedMillis = Duration.ofNanos(System.nanoTime() - started).toMillis();
			holder.rollback();
		}

		assertThat(thrown).isInstanceOf(PessimisticLockingFailureException.class);
		assertThat(elapsedMillis).isBetween(4_000L, 10_000L);
		Integer saved = jdbcTemplate.queryForObject("select count(*) from reservations where charger_id = ?",
				Integer.class, charger.getId());
		assertThat(saved).isZero();

		ResponseEntity<ApiResponseDto<Void>> response = new ExceptionAdvice()
				.handlePessimisticLockingFailure((PessimisticLockingFailureException) thrown);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody().error().code()).isEqualTo(ErrorStatus.SERVICE_BUSY.getCode());
	}

}
