package com.picka_chew.dangcharge.api.user.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.user.dto.UserResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetMyInfoUseCase {

	private final UserAdaptor userAdaptor;

	@Transactional(readOnly = true)
	public UserResponse execute(Long userId) {
		return UserResponse.from(userAdaptor.getById(userId));
	}

}
