package com.picka_chew.dangcharge.api.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.user.dto.UpdateUserRequest;
import com.picka_chew.dangcharge.api.user.dto.UserResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@Tag(name = "user", description = "[마이페이지] 내 정보")
@RestController
@RequestMapping("/api/v1/users")
public class UserApiController {

	@Operation(summary = "내 정보 조회", description = "로그인이 필요하다.")
	@GetMapping("/me")
	public ApiResponseDto<UserResponse> getMe() {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	@Operation(summary = "내 정보 수정", description = "로그인이 필요하다.")
	@PatchMapping("/me")
	public ApiResponseDto<UserResponse> updateMe(@RequestBody UpdateUserRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
