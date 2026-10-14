package com.picka_chew.dangcharge.api.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.auth.dto.AccessTokenResponse;
import com.picka_chew.dangcharge.api.auth.dto.LoginRequest;
import com.picka_chew.dangcharge.api.auth.dto.RefreshTokenRequest;
import com.picka_chew.dangcharge.api.auth.dto.SignupRequest;
import com.picka_chew.dangcharge.api.auth.dto.TokenResponse;
import com.picka_chew.dangcharge.api.auth.service.LoginUseCase;
import com.picka_chew.dangcharge.api.auth.service.LogoutUseCase;
import com.picka_chew.dangcharge.api.auth.service.RefreshTokenUseCase;
import com.picka_chew.dangcharge.api.auth.service.SignUpUseCase;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.annotation.CurrentUser;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "auth", description = "[인증] 회원가입·로그인·토큰")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthApiController {

	private final SignUpUseCase signUpUseCase;
	private final LoginUseCase loginUseCase;
	private final RefreshTokenUseCase refreshTokenUseCase;
	private final LogoutUseCase logoutUseCase;

	@Operation(summary = "회원가입", description = "비밀번호는 8~64자이며 영문과 숫자를 모두 포함해야 한다.")
	@SecurityRequirements
	@PostMapping("/signup")
	public ApiResponseDto<Void> signup(@Valid @RequestBody SignupRequest request) {
		signUpUseCase.execute(request);
		return ApiResponseDto.onSuccess(null);
	}

	@Operation(summary = "로그인", description = "access/refresh 토큰을 발급한다.")
	@SecurityRequirements
	@PostMapping("/login")
	public ApiResponseDto<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponseDto.onSuccess(loginUseCase.execute(request));
	}

	@Operation(summary = "access 토큰 재발급")
	@SecurityRequirements
	@PostMapping("/refresh")
	public ApiResponseDto<AccessTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return ApiResponseDto.onSuccess(refreshTokenUseCase.execute(request.refreshToken()));
	}

	@Operation(summary = "로그아웃", description = "refresh 토큰을 폐기하고 지금 쓰던 access 토큰을 막는다. 본문은 보내지 않아도 된다.")
	@PostMapping("/logout")
	public ApiResponseDto<Void> logout(@CurrentUser AuthUser authUser,
			@RequestBody(required = false) RefreshTokenRequest request) {
		logoutUseCase.execute(authUser);
		return ApiResponseDto.onSuccess(null);
	}

}
