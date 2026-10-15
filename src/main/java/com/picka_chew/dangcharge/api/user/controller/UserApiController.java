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
import com.picka_chew.dangcharge.api.user.service.GetMyInfoUseCase;
import com.picka_chew.dangcharge.api.user.service.UpdateMyInfoUseCase;
import com.picka_chew.dangcharge.common.annotation.CurrentUser;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "user", description = "[마이페이지] 내 정보")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserApiController {

	private final GetMyInfoUseCase getMyInfoUseCase;
	private final UpdateMyInfoUseCase updateMyInfoUseCase;

	@Operation(summary = "내 정보 조회", description = "로그인이 필요하다.")
	@GetMapping("/me")
	public ApiResponseDto<UserResponse> getMe(@CurrentUser AuthUser authUser) {
		return ApiResponseDto.onSuccess(getMyInfoUseCase.execute(authUser.userId()));
	}

	@Operation(summary = "내 정보 수정", description = "바꿀 값만 보낸다. 진행 중인 예약이 있으면 차종은 바꿀 수 없다. 로그인이 필요하다.")
	@PatchMapping("/me")
	public ApiResponseDto<UserResponse> updateMe(@CurrentUser AuthUser authUser,
			@Valid @RequestBody UpdateUserRequest request) {
		return ApiResponseDto.onSuccess(updateMyInfoUseCase.execute(authUser.userId(), request));
	}

}
