package com.picka_chew.dangcharge.domain.user.adaptor;

import java.util.Optional;

import com.picka_chew.dangcharge.common.annotation.Adaptor;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Adaptor
@RequiredArgsConstructor
public class UserAdaptorImpl implements UserAdaptor {

	private final UserRepository userRepository;

	@Override
	public User getById(Long userId) {
		return userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
	}

	@Override
	public User getByIdForUpdate(Long userId) {
		return userRepository.findByIdForUpdate(userId)
				.orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
	}

	@Override
	public Optional<User> findByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId);
	}

	@Override
	public boolean existsByLoginId(String loginId) {
		return userRepository.existsByLoginId(loginId);
	}

}
