package com.picka_chew.dangcharge.api.auth.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.auth.dto.AccessTokenResponse;
import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.api.auth.port.TokenIssuer;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;

import lombok.RequiredArgsConstructor;

/**
 * 액세스 토큰만 다시 발급한다. 응답에 리프레시 토큰이 없어 리프레시 토큰은 교체(rotation)하지 않는다.
 * 저장된 리프레시 토큰과 다르면 탈취로 보고 저장된 토큰까지 지운다.
 */
@UseCase
@RequiredArgsConstructor
public class RefreshTokenUseCase {

	private final TokenIssuer tokenIssuer;
	private final RefreshTokenStore refreshTokenStore;
	private final UserAdaptor userAdaptor;

	/** 불일치로 예외를 던져도 저장된 토큰 삭제는 커밋한다 */
	@Transactional(noRollbackFor = GeneralException.class)
	public AccessTokenResponse execute(String refreshToken) {
		Long userId = tokenIssuer.verifyRefreshToken(refreshToken);
		if (!refreshTokenStore.matches(userId, refreshToken)) {
			refreshTokenStore.delete(userId);
			throw new GeneralException(ErrorStatus.INVALID_REFRESH_TOKEN);
		}
		userAdaptor.getById(userId);
		return new AccessTokenResponse(tokenIssuer.issueAccessToken(userId));
	}

}
