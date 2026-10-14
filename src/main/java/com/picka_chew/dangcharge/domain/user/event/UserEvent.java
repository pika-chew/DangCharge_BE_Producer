package com.picka_chew.dangcharge.domain.user.event;

import java.time.Instant;

import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

/** 사용자 생성·수정 이벤트. 비밀번호 해시는 담지 않는다 */
public record UserEvent(String eventType, Long userId, String loginId, String username, VehicleModel vehicleModel,
		Instant createdAt) implements DomainEvent {

	public static UserEvent created(User user) {
		return of("UserCreated", user);
	}

	public static UserEvent updated(User user) {
		return of("UserUpdated", user);
	}

	private static UserEvent of(String eventType, User user) {
		return new UserEvent(eventType, user.getId(), user.getLoginId(), user.getUsername(), user.getVehicleModel(),
				user.getCreatedAt());
	}

	@Override
	public String aggregateType() {
		return "user";
	}

	@Override
	public Long aggregateId() {
		return userId;
	}

}
