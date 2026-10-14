package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationResponse;
import com.picka_chew.dangcharge.api.reservation.port.IdempotencyStore;
import com.picka_chew.dangcharge.common.annotation.UseCase;
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
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 예약 생성. 겹침 확인 후 저장은 check-then-act라 락 없이는 동시 요청이 함께 통과한다.
 * 사용자 행과 충전기 행을 이 순서로 FOR UPDATE로 잠가 사용자 단위(진행 중 예약 1개)와
 * 충전기 단위(시간 겹침)로 직렬화한다. 다른 충전기·다른 사용자의 예약은 막히지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class CreateReservationUseCase {

	private final IdempotencyStore idempotencyStore;
	private final UserAdaptor userAdaptor;
	private final ChargerAdaptor chargerAdaptor;
	private final ReservationAdaptor reservationAdaptor;
	private final ReservationValidator reservationValidator;
	private final ReservationDomainService reservationDomainService;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public ReservationResponse execute(Long userId, CreateReservationRequest request, String idempotencyKey) {
		if (idempotencyKey != null && !idempotencyKey.isBlank()) {
			if (!idempotencyStore.tryAcquire(userId, idempotencyKey)) {
				Long reservationId = idempotencyStore.findCompletedReservationId(userId, idempotencyKey)
						.orElseThrow(() -> new GeneralException(ErrorStatus.DUPLICATED_REQUEST));
				return ReservationResponse.from(reservationAdaptor.getByIdAndUserId(reservationId, userId));
			}
		}
		IdempotencyResult idempotency = registerIdempotency(userId, idempotencyKey);

		Instant now = Instant.now(clock);
		Instant startsAt = request.startAt().toInstant();
		Instant endsAt = request.endAt().toInstant();
		reservationValidator.validateTimeRange(startsAt, endsAt, now);

		User user = userAdaptor.getByIdForUpdate(userId);
		reservationValidator.validateNoActiveReservation(userId, now);
		Charger charger = chargerAdaptor.getByIdForUpdate(request.chargerId());
		reservationValidator.validateChargerUsable(charger);
		reservationValidator.validateVehicleCompatible(charger.getStation(), user.getVehicleModel());
		reservationValidator.validateNoOverlap(charger.getId(), startsAt, endsAt);

		Reservation reservation = reservationDomainService.create(user, charger, startsAt, endsAt);
		eventRecorder.record(ReservationEvent.created(reservation));
		idempotency.reservationId = reservation.getId();
		return ReservationResponse.from(reservation);
	}

	/** 커밋되면 키에 예약 ID를 남기고, 롤백되면 키를 풀어 같은 키로 다시 시도할 수 있게 한다 */
	private IdempotencyResult registerIdempotency(Long userId, String idempotencyKey) {
		IdempotencyResult result = new IdempotencyResult();
		if (idempotencyKey == null || idempotencyKey.isBlank()) {
			return result;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_COMMITTED && result.reservationId != null) {
					idempotencyStore.complete(userId, idempotencyKey, result.reservationId);
				}
				else {
					idempotencyStore.release(userId, idempotencyKey);
				}
			}
		});
		return result;
	}

	private static final class IdempotencyResult {

		private Long reservationId;

	}

}
