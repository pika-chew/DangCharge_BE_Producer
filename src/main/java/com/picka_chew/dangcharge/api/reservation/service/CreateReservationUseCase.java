package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

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
 * 사용자 행과 충전기 행을 이 순서로 FOR UPDATE로 잠가 사용자 단위(진행 중 예약 1개, 같은 Idempotency-Key)와
 * 충전기 단위(시간 겹침)로 직렬화한다. 다른 충전기·다른 사용자의 예약은 막히지 않는다.
 */
@UseCase
@RequiredArgsConstructor
public class CreateReservationUseCase {

	private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 100;

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
		boolean idempotent = idempotencyKey != null && !idempotencyKey.isBlank();
		if (idempotent && idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
			throw new GeneralException(ErrorStatus.BAD_REQUEST);
		}
		Instant now = Instant.now(clock);
		Instant startsAt = request.startAt().toInstant();
		Instant endsAt = request.endAt().toInstant();

		User user = userAdaptor.getByIdForUpdate(userId);
		if (idempotent) {
			// 사용자 행 락 뒤에 확인하므로 같은 키로 동시에 와도 두 번째 요청은 첫 요청이 끝난 뒤 그 결과를 본다
			Long existingId = idempotencyStore.findReservationId(userId, idempotencyKey).orElse(null);
			if (existingId != null) {
				return ReservationResponse.from(reservationAdaptor.getByIdAndUserId(existingId, userId));
			}
		}

		reservationValidator.validateTimeRange(startsAt, endsAt, now);
		reservationValidator.validateNoActiveReservation(userId, now);
		Charger charger = chargerAdaptor.getByIdForUpdate(request.chargerId());
		reservationValidator.validateChargerUsable(charger);
		reservationValidator.validateVehicleCompatible(charger.getStation(), user.getVehicleModel());
		reservationValidator.validateNoOverlap(charger.getId(), startsAt, endsAt);

		Reservation reservation = reservationDomainService.create(user, charger, startsAt, endsAt);
		eventRecorder.record(ReservationEvent.created(reservation));
		if (idempotent) {
			idempotencyStore.save(userId, idempotencyKey, reservation.getId());
		}
		return ReservationResponse.from(reservation);
	}

}
