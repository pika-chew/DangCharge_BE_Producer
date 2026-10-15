package com.picka_chew.dangcharge.api.user.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.user.dto.UpdateUserRequest;
import com.picka_chew.dangcharge.api.user.dto.UserResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.event.UserEvent;
import com.picka_chew.dangcharge.domain.user.service.UserDomainService;

import lombok.RequiredArgsConstructor;

/** 진행 중인 예약이 있으면 차종을 바꿀 수 없다. 예약한 충전소가 새 차종을 지원하지 않을 수 있기 때문이다 */
@UseCase
@RequiredArgsConstructor
public class UpdateMyInfoUseCase {

	private final UserAdaptor userAdaptor;
	private final UserDomainService userDomainService;
	private final ReservationAdaptor reservationAdaptor;
	private final DomainEventRecorder eventRecorder;
	private final Clock clock;

	@Transactional
	public UserResponse execute(Long userId, UpdateUserRequest request) {
		User user = userAdaptor.getById(userId);
		boolean modelChanges = request.vehicleModel() != null && request.vehicleModel() != user.getVehicleModel();
		if (modelChanges && reservationAdaptor.existsActiveByUserId(userId, Instant.now(clock))) {
			throw new GeneralException(ErrorStatus.VEHICLE_IN_USE);
		}
		String username = request.username() == null ? null : request.username().strip();
		userDomainService.updateProfile(user, username, request.vehicleModel());
		eventRecorder.record(UserEvent.updated(user));
		return UserResponse.from(user);
	}

}
