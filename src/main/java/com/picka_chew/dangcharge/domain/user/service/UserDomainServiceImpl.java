package com.picka_chew.dangcharge.domain.user.service;

import com.picka_chew.dangcharge.common.annotation.DomainService;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import lombok.RequiredArgsConstructor;

/** 트랜잭션은 호출하는 UseCase가 연다 */
@DomainService
@RequiredArgsConstructor
public class UserDomainServiceImpl implements UserDomainService {

	private final UserRepository userRepository;

	@Override
	public User create(String loginId, String encodedPassword, String username, VehicleModel vehicleModel) {
		return userRepository.save(User.builder()
				.loginId(loginId)
				.passwordHash(encodedPassword)
				.username(username)
				.vehicleModel(vehicleModel)
				.build());
	}

	@Override
	public void updateProfile(User user, String username, VehicleModel vehicleModel) {
		user.updateProfile(username, vehicleModel);
	}

}
