package com.picka_chew.dangcharge.api.auth.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.auth.dto.LoginRequest;
import com.picka_chew.dangcharge.api.auth.dto.TokenResponse;
import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.api.auth.port.TokenIssuer;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.validator.UserValidator;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class LoginUseCase {

	private final UserAdaptor userAdaptor;
	private final UserValidator userValidator;
	private final TokenIssuer tokenIssuer;
	private final RefreshTokenStore refreshTokenStore;

	/** 아이디가 없어도, 비밀번호가 틀려도 같은 오류(INVALID_CREDENTIALS)를 돌려준다 */
	@Transactional(readOnly = true)
	public TokenResponse execute(LoginRequest request) {
		User user = userAdaptor.findByLoginId(request.loginId())
				.orElseThrow(() -> new GeneralException(ErrorStatus.INVALID_CREDENTIALS));
		userValidator.validatePasswordMatch(request.password(), user.getPasswordHash());

		String accessToken = tokenIssuer.issueAccessToken(user.getId());
		String refreshToken = tokenIssuer.issueRefreshToken(user.getId());
		refreshTokenStore.save(user.getId(), refreshToken, tokenIssuer.refreshTokenTtl());
		return new TokenResponse(accessToken, refreshToken);
	}

}
