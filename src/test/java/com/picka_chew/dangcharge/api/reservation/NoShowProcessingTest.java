package com.picka_chew.dangcharge.api.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import com.picka_chew.dangcharge.api.reservation.service.MarkNoShowReservationsUseCase;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.repository.ReservationRepository;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/**
 * 충전 시작 마감(예약 시작 15분 후)이 지난 CONFIRMED 예약만 NO_SHOW가 되고,
 * 그 뒤 사용자와 충전기 시간이 풀리는지 실제 MySQL로 확인한다.
 * 스케줄러는 끄고 UseCase를 직접 부른다. 커밋된 데이터를 봐야 해서 테스트 데이터를 직접 지운다.
 */
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false",
		"dangcharge.no-show.enabled=false"})
class NoShowProcessingTest {

	@Autowired
	MarkNoShowReservationsUseCase markNoShowReservationsUseCase;
	@Autowired
	ReservationAdaptor reservationAdaptor;
	@Autowired
	ReservationRepository reservationRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	StationRepository stationRepository;
	@Autowired
	ChargerRepository chargerRepository;
	@Autowired
	TransactionTemplate transactionTemplate;
	@Autowired
	JdbcTemplate jdbcTemplate;

	Station station;
	Charger charger;
	User noShowUser;
	User onTimeUser;
	User chargingUser;

	@BeforeEach
	void setUp() {
		String suffix = String.valueOf(System.nanoTime());
		station = stationRepository.save(Station.builder()
				.name("노쇼테스트-" + suffix).address("테스트주소-" + suffix).metro("테스트").city("테스트")
				.rapidCount(1).slowCount(0).supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build());
		charger = chargerRepository.save(Charger.simulated(station, ChargingType.FAST));
		noShowUser = user("noshow-" + suffix);
		onTimeUser = user("ontime-" + suffix);
		chargingUser = user("charging-" + suffix);
	}

	@AfterEach
	void cleanUp() {
		jdbcTemplate.update("delete from outbox_events where aggregate_type = 'reservation' and payload like ?",
				"%" + station.getName() + "%");
		jdbcTemplate.update("delete from reservations where charger_id = ?", charger.getId());
		jdbcTemplate.update("delete from chargers where id = ?", charger.getId());
		jdbcTemplate.update("delete from station_supported_models where station_id = ?", station.getId());
		jdbcTemplate.update("delete from stations where id = ?", station.getId());
		for (User user : new User[] {noShowUser, onTimeUser, chargingUser}) {
			jdbcTemplate.update("delete from users where id = ?", user.getId());
		}
	}

	@Test
	void onlyReservationsPastCheckInDeadlineBecomeNoShow() {
		Instant now = Instant.now();
		Reservation missed = reservation(noShowUser, now.minus(Duration.ofMinutes(30)));
		Reservation stillInWindow = reservation(onTimeUser, now.minus(Duration.ofMinutes(10)));
		Reservation charging = reservation(chargingUser, now.minus(Duration.ofMinutes(40)));
		transactionTemplate.executeWithoutResult(status -> {
			Reservation managed = reservationRepository.findById(charging.getId()).orElseThrow();
			managed.startCharging(now.minus(Duration.ofMinutes(35)));
		});

		markNoShowReservationsUseCase.execute();

		assertThat(statusOf(missed)).isEqualTo("NO_SHOW");
		assertThat(statusOf(stillInWindow)).isEqualTo("CONFIRMED");
		assertThat(statusOf(charging)).isEqualTo("CHARGING");
		assertThat(jdbcTemplate.queryForObject("""
				select count(*) from outbox_events
				where aggregate_type = 'reservation' and aggregate_id = ? and event_type = 'ReservationNoShow'
				""", Integer.class, missed.getId())).isEqualTo(1);
	}

	@Test
	void noShowFreesUserAndChargerTime() {
		Instant now = Instant.now();
		Reservation missed = reservation(noShowUser, now.minus(Duration.ofMinutes(30)));
		assertThat(reservationAdaptor.existsActiveByUserId(noShowUser.getId(), now)).isTrue();
		assertThat(reservationAdaptor.existsOverlap(charger.getId(), missed.getStartsAt(), missed.getEndsAt()))
				.isTrue();

		markNoShowReservationsUseCase.execute();

		assertThat(reservationAdaptor.existsActiveByUserId(noShowUser.getId(), now)).isFalse();
		assertThat(reservationAdaptor.existsOverlap(charger.getId(), missed.getStartsAt(), missed.getEndsAt()))
				.isFalse();
	}

	@Test
	void runningAgainDoesNotProcessSameReservationTwice() {
		Reservation missed = reservation(noShowUser, Instant.now().minus(Duration.ofMinutes(30)));

		markNoShowReservationsUseCase.execute();
		markNoShowReservationsUseCase.execute();

		assertThat(jdbcTemplate.queryForObject("""
				select count(*) from outbox_events
				where aggregate_type = 'reservation' and aggregate_id = ? and event_type = 'ReservationNoShow'
				""", Integer.class, missed.getId())).isEqualTo(1);
	}

	private User user(String loginId) {
		return userRepository.save(User.builder().loginId(loginId).passwordHash("hash").username("노쇼")
				.vehicleModel(VehicleModel.TESLA).build());
	}

	private Reservation reservation(User user, Instant startsAt) {
		return reservationRepository.save(Reservation.builder().user(user).charger(charger)
				.startsAt(startsAt).endsAt(startsAt.plus(Duration.ofHours(1))).vehicleModel(VehicleModel.TESLA).build());
	}

	private String statusOf(Reservation reservation) {
		return jdbcTemplate.queryForObject("select status from reservations where id = ?", String.class,
				reservation.getId());
	}

}
