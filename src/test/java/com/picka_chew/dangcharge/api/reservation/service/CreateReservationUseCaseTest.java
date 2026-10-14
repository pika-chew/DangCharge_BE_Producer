package com.picka_chew.dangcharge.api.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.api.reservation.port.IdempotencyStore;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.event.ReservationEvent;
import com.picka_chew.dangcharge.domain.reservation.service.ReservationDomainService;
import com.picka_chew.dangcharge.domain.reservation.validator.ReservationValidator;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

@ExtendWith(MockitoExtension.class)
class CreateReservationUseCaseTest {

	private static final Instant NOW = Instant.parse("2026-10-14T01:00:00Z");
	private static final CreateReservationRequest REQUEST = new CreateReservationRequest(10L, null,
			OffsetDateTime.parse("2026-10-14T14:00:00+09:00"), OffsetDateTime.parse("2026-10-14T15:00:00+09:00"));

	@Mock
	IdempotencyStore idempotencyStore;
	@Mock
	UserAdaptor userAdaptor;
	@Mock
	ChargerAdaptor chargerAdaptor;
	@Mock
	ReservationAdaptor reservationAdaptor;
	@Mock
	ReservationValidator reservationValidator;
	@Mock
	ReservationDomainService reservationDomainService;
	@Mock
	DomainEventRecorder eventRecorder;

	CreateReservationUseCase useCase;
	User user;
	Charger charger;

	@BeforeEach
	void setUp() {
		useCase = new CreateReservationUseCase(idempotencyStore, userAdaptor, chargerAdaptor, reservationAdaptor,
				reservationValidator, reservationDomainService, eventRecorder, Clock.fixed(NOW, ZoneOffset.UTC));
		user = User.builder().id(1L).vehicleModel(VehicleModel.TESLA).build();
		Station station = Station.builder().id(100L).name("강남 충전소")
				.supportedModels(new HashSet<>(Set.of(VehicleModel.TESLA))).build();
		charger = Charger.builder().id(10L).station(station).build();
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void tearDown() {
		TransactionSynchronizationManager.clearSynchronization();
	}

	@Test
	void locksUserThenChargerBeforeCheckingOverlapAndRecordsEvent() {
		given(userAdaptor.getByIdForUpdate(1L)).willReturn(user);
		given(chargerAdaptor.getByIdForUpdate(10L)).willReturn(charger);
		Reservation saved = reservation(55L);
		given(reservationDomainService.create(user, charger, REQUEST.startAt().toInstant(), REQUEST.endAt().toInstant()))
				.willReturn(saved);

		ReservationResponse response = useCase.execute(1L, REQUEST, null);

		assertThat(response.reservationId()).isEqualTo(55L);
		InOrder order = inOrder(reservationValidator, userAdaptor, chargerAdaptor, reservationDomainService, eventRecorder);
		order.verify(reservationValidator).validateTimeRange(any(), any(), any());
		order.verify(userAdaptor).getByIdForUpdate(1L);
		order.verify(reservationValidator).validateNoActiveReservation(1L, NOW);
		order.verify(chargerAdaptor).getByIdForUpdate(10L);
		order.verify(reservationValidator).validateNoOverlap(10L, REQUEST.startAt().toInstant(),
				REQUEST.endAt().toInstant());
		order.verify(reservationDomainService).create(any(), any(), any(), any());
		order.verify(eventRecorder).record(any(ReservationEvent.class));
		verifyNoInteractions(idempotencyStore);
	}

	@Test
	void conflictStopsBeforeSaving() {
		given(userAdaptor.getByIdForUpdate(1L)).willReturn(user);
		given(chargerAdaptor.getByIdForUpdate(10L)).willReturn(charger);
		willThrow(new GeneralException(ErrorStatus.RESERVATION_CONFLICT))
				.given(reservationValidator).validateNoOverlap(any(), any(), any());

		assertThatThrownBy(() -> useCase.execute(1L, REQUEST, null))
				.extracting("errorStatus").isEqualTo(ErrorStatus.RESERVATION_CONFLICT);
		verify(reservationDomainService, never()).create(any(), any(), any(), any());
		verifyNoInteractions(eventRecorder);
	}

	@Test
	void repeatedKeyReturnsReservationCreatedFirst() {
		given(idempotencyStore.tryAcquire(1L, "key")).willReturn(false);
		given(idempotencyStore.findCompletedReservationId(1L, "key")).willReturn(Optional.of(55L));
		given(reservationAdaptor.getByIdAndUserId(55L, 1L)).willReturn(reservation(55L));

		ReservationResponse response = useCase.execute(1L, REQUEST, "key");

		assertThat(response.reservationId()).isEqualTo(55L);
		verifyNoInteractions(reservationDomainService, eventRecorder);
	}

	@Test
	void keyStillBeingProcessedIsDuplicatedRequest() {
		given(idempotencyStore.tryAcquire(1L, "key")).willReturn(false);
		given(idempotencyStore.findCompletedReservationId(1L, "key")).willReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(1L, REQUEST, "key"))
				.extracting("errorStatus").isEqualTo(ErrorStatus.DUPLICATED_REQUEST);
	}

	@Test
	void keyIsSavedOnCommitAndReleasedOnRollback() {
		given(idempotencyStore.tryAcquire(1L, "key")).willReturn(true);
		given(userAdaptor.getByIdForUpdate(1L)).willReturn(user);
		given(chargerAdaptor.getByIdForUpdate(10L)).willReturn(charger);
		given(reservationDomainService.create(any(), any(), any(), any())).willReturn(reservation(55L));

		useCase.execute(1L, REQUEST, "key");
		completeTransaction(TransactionSynchronization.STATUS_COMMITTED);
		verify(idempotencyStore).complete(1L, "key", 55L);

		TransactionSynchronizationManager.clearSynchronization();
		TransactionSynchronizationManager.initSynchronization();
		willThrow(new GeneralException(ErrorStatus.RESERVATION_CONFLICT))
				.given(reservationValidator).validateNoOverlap(any(), any(), any());
		assertThatThrownBy(() -> useCase.execute(1L, REQUEST, "key")).isInstanceOf(GeneralException.class);
		completeTransaction(TransactionSynchronization.STATUS_ROLLED_BACK);
		verify(idempotencyStore).release(1L, "key");
	}

	private Reservation reservation(Long id) {
		return Reservation.builder().id(id).user(user).charger(charger)
				.startsAt(REQUEST.startAt().toInstant()).endsAt(REQUEST.endAt().toInstant()).build();
	}

	private static void completeTransaction(int status) {
		TransactionSynchronizationManager.getSynchronizations().forEach(sync -> sync.afterCompletion(status));
	}

}
