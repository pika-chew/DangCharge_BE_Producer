package com.picka_chew.dangcharge.api.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
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
 * 여러 사용자가 같은 충전기·같은 시간을 동시에 예약하면 하나만 저장되는지 실제 MySQL로 확인한다.
 * 스레드마다 커밋해야 해서 롤백 대신 테스트가 만든 데이터를 직접 지운다.
 */
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false",
		"dangcharge.no-show.enabled=false"})
class CreateReservationConcurrencyTest {

	private static final int USERS = 8;

	@Autowired
	CreateReservationUseCase createReservationUseCase;
	@Autowired
	UserRepository userRepository;
	@Autowired
	StationRepository stationRepository;
	@Autowired
	ChargerRepository chargerRepository;
	@Autowired
	JdbcTemplate jdbcTemplate;

	Station station;
	Charger charger;
	List<Long> userIds = new ArrayList<>();

	@BeforeEach
	void setUp() {
		String suffix = String.valueOf(System.nanoTime());
		station = stationRepository.save(Station.builder()
				.name("동시성테스트-" + suffix).address("테스트주소-" + suffix).metro("테스트").city("테스트")
				.rapidCount(1).slowCount(0).supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build());
		charger = chargerRepository.save(Charger.simulated(station, ChargingType.FAST));
		for (int i = 0; i < USERS; i++) {
			userIds.add(userRepository.save(User.builder()
					.loginId("concurrency-" + suffix + "-" + i).passwordHash("hash").username("동시성" + i)
					.vehicleModel(VehicleModel.TESLA).build()).getId());
		}
	}

	@AfterEach
	void cleanUp() {
		jdbcTemplate.update("delete from outbox_events where aggregate_type = 'reservation' and aggregate_id in "
				+ "(select id from reservations where charger_id = ?)", charger.getId());
		jdbcTemplate.update("delete from reservations where charger_id = ?", charger.getId());
		jdbcTemplate.update("delete from chargers where id = ?", charger.getId());
		jdbcTemplate.update("delete from station_supported_models where station_id = ?", station.getId());
		jdbcTemplate.update("delete from stations where id = ?", station.getId());
		userIds.forEach(id -> jdbcTemplate.update("delete from users where id = ?", id));
	}

	@Test
	void onlyOneOfConcurrentRequestsForSameChargerAndTimeIsSaved() throws Exception {
		OffsetDateTime startAt = LocalDate.now(TimeUtils.ZONE).plusDays(1).atTime(10, 0)
				.atZone(TimeUtils.ZONE).toOffsetDateTime();
		CreateReservationRequest request = new CreateReservationRequest(charger.getId(), startAt);

		ExecutorService executor = Executors.newFixedThreadPool(USERS);
		CountDownLatch ready = new CountDownLatch(USERS);
		CountDownLatch go = new CountDownLatch(1);
		AtomicInteger conflicts = new AtomicInteger();
		List<Future<?>> futures = new ArrayList<>();
		for (Long userId : userIds) {
			futures.add(executor.submit(() -> {
				ready.countDown();
				go.await();
				try {
					createReservationUseCase.execute(userId, request, null);
				}
				catch (GeneralException exception) {
					if (exception.getErrorStatus() == ErrorStatus.RESERVATION_CONFLICT) {
						conflicts.incrementAndGet();
					}
					else {
						throw exception;
					}
				}
				return null;
			}));
		}
		ready.await();
		go.countDown();
		for (Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();

		Integer saved = jdbcTemplate.queryForObject(
				"select count(*) from reservations where charger_id = ? and status = 'CONFIRMED'", Integer.class,
				charger.getId());
		assertThat(saved).isEqualTo(1);
		assertThat(conflicts.get()).isEqualTo(USERS - 1);
	}

}
