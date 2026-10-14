package com.picka_chew.dangcharge.api.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.user.dto.UpdateUserRequest;
import com.picka_chew.dangcharge.api.user.dto.UserResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

	/** 내 정보 조회. 로그인 필요 */
	@GetMapping("/me")
	public ApiResponseDto<UserResponse> getMe() {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 내 정보 수정. 로그인 필요 */
	@PatchMapping("/me")
	public ApiResponseDto<UserResponse> updateMe(@RequestBody UpdateUserRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
