package com.picka_chew.dangcharge.api.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.auth.dto.SignupRequest;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.event.UserEvent;
import com.picka_chew.dangcharge.domain.user.service.UserDomainService;
import com.picka_chew.dangcharge.domain.user.validator.UserValidator;

import lombok.RequiredArgsConstructor;

/**
 * 동시에 같은 아이디로 가입하면 둘 다 중복 검증을 통과할 수 있다. 이때는 users.login_id 유니크 제약이 막고,
 * ExceptionAdvice가 DUPLICATED_LOGIN_ID로 바꾼다.
 */
@UseCase
@RequiredArgsConstructor
public class SignUpUseCase {

	private final UserValidator userValidator;
	private final UserDomainService userDomainService;
	private final PasswordEncoder passwordEncoder;
	private final DomainEventRecorder eventRecorder;

	@Transactional
	public void execute(SignupRequest request) {
		userValidator.validatePasswordPolicy(request.password());
		userValidator.validateLoginIdNotDuplicated(request.loginId());
		User user = userDomainService.create(request.loginId(), passwordEncoder.encode(request.password()),
				request.username().strip(), request.vehicleModel());
		eventRecorder.record(UserEvent.created(user));
	}

}
