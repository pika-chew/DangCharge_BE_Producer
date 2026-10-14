package com.picka_chew.dangcharge.api.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.auth.dto.AccessTokenResponse;
import com.picka_chew.dangcharge.api.auth.dto.LoginRequest;
import com.picka_chew.dangcharge.api.auth.dto.RefreshTokenRequest;
import com.picka_chew.dangcharge.api.auth.dto.SignupRequest;
import com.picka_chew.dangcharge.api.auth.dto.TokenResponse;
import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

	/** 회원가입 */
	@PostMapping("/signup")
	public ApiResponseDto<Void> signup(@RequestBody SignupRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 로그인. access/refresh 토큰을 발급한다 */
	@PostMapping("/login")
	public ApiResponseDto<TokenResponse> login(@RequestBody LoginRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** access 토큰 재발급 */
	@PostMapping("/refresh")
	public ApiResponseDto<AccessTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 로그아웃. refresh 토큰을 폐기한다. 로그인 필요 */
	@PostMapping("/logout")
	public ApiResponseDto<Void> logout(@RequestBody RefreshTokenRequest request) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
