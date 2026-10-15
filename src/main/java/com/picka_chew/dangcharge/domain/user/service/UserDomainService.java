package com.picka_chew.dangcharge.domain.user.service;

import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public interface UserDomainService {

	User create(String loginId, String encodedPassword, String username, VehicleModel vehicleModel);

	void updateProfile(User user, String username, VehicleModel vehicleModel);

}
