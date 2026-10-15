package com.picka_chew.dangcharge.domain.user.validator;

public interface UserValidator {

	/** 이미 쓰는 아이디면 DUPLICATED_LOGIN_ID */
	void validateLoginIdNotDuplicated(String loginId);

	/** 8~64자, 영문과 숫자를 모두 포함해야 한다. 아니면 INVALID_PASSWORD_POLICY */
	void validatePasswordPolicy(String rawPassword);

	/** 일치하지 않으면 INVALID_CREDENTIALS */
	void validatePasswordMatch(String rawPassword, String encodedPassword);

}
