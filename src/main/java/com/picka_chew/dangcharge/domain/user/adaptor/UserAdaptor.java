package com.picka_chew.dangcharge.domain.user.adaptor;

import java.util.Optional;

import com.picka_chew.dangcharge.domain.user.entity.User;

public interface UserAdaptor {

	/** 없으면 USER_NOT_FOUND */
	User getById(Long userId);

	/** 비관적 락으로 조회한다. 한 사용자의 예약 생성을 직렬화할 때 쓴다. 없으면 USER_NOT_FOUND */
	User getByIdForUpdate(Long userId);

	Optional<User> findByLoginId(String loginId);

	boolean existsByLoginId(String loginId);

}
