package com.picka_chew.dangcharge.api.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.service.CreateReservationUseCase;
import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.common.util.TimeUtils;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.ChargingType;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.station.repository.ChargerRepository;
import com.picka_chew.dangcharge.domain.station.repository.StationRepository;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;
import com.picka_chew.dangcharge.infra.outbox.OutboxRecorder;

/**
 * PRD-01, PRD-02: 예약과 outbox 이벤트가 같은 트랜잭션으로 저장되는지 실제 MySQL로 확인한다.
 * 이벤트 기록 앞뒤에서 예외를 일으켜, 예약과 이벤트가 둘 다 남지 않는지(원자성) 본다.
 * 커밋된 데이터를 확인해야 해서 롤백 대신 테스트가 만든 데이터를 직접 지운다.
 */
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false"})
@Import(ReservationOutboxAtomicityTest.FailingRecorderConfig.class)
class ReservationOutboxAtomicityTest {

	@Autowired
	CreateReservationUseCase createReservationUseCase;
	@Autowired
	FailingRecorder recorder;
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
	Long userId;
	CreateReservationRequest request;

	@BeforeEach
	void setUp() {
		String suffix = String.valueOf(System.nanoTime());
		station = stationRepository.save(Station.builder()
				.name("원자성테스트-" + suffix).address("테스트주소-" + suffix).metro("테스트").city("테스트")
				.rapidCount(1).slowCount(0).supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build());
		charger = chargerRepository.save(Charger.simulated(station, ChargingType.FAST));
		userId = userRepository.save(User.builder()
				.loginId("atomicity-" + suffix).passwordHash("hash").username("원자성")
				.vehicleModel(VehicleModel.TESLA).build()).getId();
		OffsetDateTime startAt = LocalDate.now(TimeUtils.ZONE).plusDays(1).atTime(10, 0)
				.atZone(TimeUtils.ZONE).toOffsetDateTime();
		request = new CreateReservationRequest(charger.getId(), startAt);
		recorder.mode = FailMode.NONE;
	}

	@AfterEach
	void cleanUp() {
		recorder.mode = FailMode.NONE;
		jdbcTemplate.update("delete from outbox_events where aggregate_type = 'reservation' and payload like ?",
				"%" + station.getName() + "%");
		jdbcTemplate.update("delete from reservations where charger_id = ?", charger.getId());
		jdbcTemplate.update("delete from chargers where id = ?", charger.getId());
		jdbcTemplate.update("delete from station_supported_models where station_id = ?", station.getId());
		jdbcTemplate.update("delete from stations where id = ?", station.getId());
		jdbcTemplate.update("delete from users where id = ?", userId);
	}

	@Test
	void prd01_reservationAndEventAreSavedTogether() {
		Long reservationId = createReservationUseCase.execute(userId, request, null).reservationId();

		assertThat(reservationCount()).isEqualTo(1);
		assertThat(jdbcTemplate.queryForObject("""
				select count(*) from outbox_events
				where aggregate_type = 'reservation' and aggregate_id = ? and event_type = 'ReservationCreated'
				and sent_at is null
				""", Integer.class, reservationId)).isEqualTo(1);
	}

	@Test
	void prd02_failureBeforeEventIsWrittenLeavesNothing() {
		recorder.mode = FailMode.BEFORE_WRITE;

		assertThatThrownBy(() -> createReservationUseCase.execute(userId, request, null))
				.isInstanceOf(IllegalStateException.class);

		assertThat(reservationCount()).isZero();
		assertThat(eventCount()).isZero();
	}

	@Test
	void prd02_failureAfterEventIsWrittenRollsBackBoth() {
		recorder.mode = FailMode.AFTER_WRITE;

		assertThatThrownBy(() -> createReservationUseCase.execute(userId, request, null))
				.isInstanceOf(IllegalStateException.class);

		assertThat(recorder.wroteBeforeFailure).isTrue();
		assertThat(reservationCount()).isZero();
		assertThat(eventCount()).isZero();
	}

	private Integer reservationCount() {
		return jdbcTemplate.queryForObject("select count(*) from reservations where charger_id = ?", Integer.class,
				charger.getId());
	}

	/** 실패한 예약은 ID가 남지 않으므로, 이 테스트 충전소 이름이 들어간 예약 이벤트를 센다 */
	private Integer eventCount() {
		return jdbcTemplate.queryForObject(
				"select count(*) from outbox_events where aggregate_type = 'reservation' and payload like ?",
				Integer.class, "%" + station.getName() + "%");
	}

	enum FailMode {
		NONE, BEFORE_WRITE, AFTER_WRITE
	}

	/** 실제 OutboxRecorder로 기록하되, 정한 시점에 예외를 던진다 */
	static class FailingRecorder implements DomainEventRecorder {

		private final OutboxRecorder delegate;
		volatile FailMode mode = FailMode.NONE;
		volatile boolean wroteBeforeFailure;

		FailingRecorder(OutboxRecorder delegate) {
			this.delegate = delegate;
		}

		@Override
		public void record(DomainEvent event) {
			wroteBeforeFailure = false;
			if (mode == FailMode.BEFORE_WRITE) {
				throw new IllegalStateException("outbox 저장 직전 강제 실패");
			}
			delegate.record(event);
			if (mode == FailMode.AFTER_WRITE) {
				wroteBeforeFailure = true;
				throw new IllegalStateException("outbox 저장 직후 강제 실패");
			}
		}

	}

	@TestConfiguration
	static class FailingRecorderConfig {

		@Bean
		@Primary
		FailingRecorder failingRecorder(OutboxRecorder outboxRecorder) {
			return new FailingRecorder(outboxRecorder);
		}

	}

}
