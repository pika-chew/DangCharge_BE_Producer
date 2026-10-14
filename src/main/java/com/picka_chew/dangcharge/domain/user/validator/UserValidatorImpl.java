package com.picka_chew.dangcharge.domain.user.validator;

import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.picka_chew.dangcharge.common.annotation.Validator;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;

import lombok.RequiredArgsConstructor;

@Validator
@RequiredArgsConstructor
public class UserValidatorImpl implements UserValidator {

	private static final Pattern LETTER = Pattern.compile("[A-Za-z]");
	private static final Pattern DIGIT = Pattern.compile("\\d");

	private final UserAdaptor userAdaptor;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void validateLoginIdNotDuplicated(String loginId) {
		if (userAdaptor.existsByLoginId(loginId)) {
			throw new GeneralException(ErrorStatus.DUPLICATED_LOGIN_ID);
		}
	}

	@Override
	public void validatePasswordPolicy(String rawPassword) {
		if (rawPassword == null || rawPassword.length() < 8 || rawPassword.length() > 64
				|| !LETTER.matcher(rawPassword).find() || !DIGIT.matcher(rawPassword).find()) {
			throw new GeneralException(ErrorStatus.INVALID_PASSWORD_POLICY);
		}
	}

	@Override
	public void validatePasswordMatch(String rawPassword, String encodedPassword) {
		if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
			throw new GeneralException(ErrorStatus.INVALID_CREDENTIALS);
		}
	}

}
