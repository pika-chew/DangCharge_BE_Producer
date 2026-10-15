package com.picka_chew.dangcharge.api.reservation.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.reservation.dto.CreateReservationRequest;
import com.picka_chew.dangcharge.api.reservation.dto.ReservationDetailResponse;
import com.picka_chew.dangcharge.api.reservation.port.IdempotencyStore;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.event.ReservationEvent;
import com.picka_chew.dangcharge.domain.reservation.policy.ReservationTimePolicy;
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
 * <p>
 * MySQL 기본 격리 수준(REPEATABLE READ)은 락 없는 첫 조회 시점의 스냅샷을 끝까지 본다. 락을 기다리기 전에
 * 스냅샷이 만들어지면, 앞 요청이 커밋한 예약이 겹침 확인에서 보이지 않아 중복 예약이 저장된다.
 * 그래서 두 락을 먼저 잡고 나서 검증하고, 격리 수준도 READ_COMMITTED로 둬 조회마다 최신 커밋을 보게 한다.
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

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public ReservationDetailResponse execute(Long userId, CreateReservationRequest request, String idempotencyKey) {
		boolean idempotent = idempotencyKey != null && !idempotencyKey.isBlank();
		if (idempotent && idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
			throw new GeneralException(ErrorStatus.BAD_REQUEST);
		}
		Instant now = Instant.now(clock);
		Instant startsAt = request.startAt().toInstant();
		Instant endsAt = ReservationTimePolicy.endOf(startsAt);

		// 락을 모두 잡은 뒤에 락 없는 조회를 한다. 락을 기다리는 동안 다른 요청이 커밋한 예약까지 보여야 하기 때문이다
		User user = userAdaptor.getByIdForUpdate(userId);
		Charger charger = chargerAdaptor.getByIdForUpdate(request.chargerId());

		if (idempotent) {
			Long existingId = idempotencyStore.findReservationId(userId, idempotencyKey).orElse(null);
			if (existingId != null) {
				return ReservationDetailResponse.from(reservationAdaptor.getByIdAndUserId(existingId, userId));
			}
		}
		reservationValidator.validateStartTime(startsAt, now);
		reservationValidator.validateNoActiveReservation(userId, now);
		reservationValidator.validateChargerUsable(charger);
		reservationValidator.validateVehicleCompatible(charger.getStation(), user.getVehicleModel());
		reservationValidator.validateNoOverlap(charger.getId(), startsAt, endsAt);

		Reservation reservation = reservationDomainService.create(user, charger, startsAt, endsAt);
		eventRecorder.record(ReservationEvent.created(reservation));
		if (idempotent) {
			idempotencyStore.save(userId, idempotencyKey, reservation.getId());
		}
		return ReservationDetailResponse.from(reservation);
	}

}
