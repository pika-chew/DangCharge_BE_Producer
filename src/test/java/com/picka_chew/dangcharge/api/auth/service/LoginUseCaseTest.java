package com.picka_chew.dangcharge.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.api.auth.dto.LoginRequest;
import com.picka_chew.dangcharge.api.auth.dto.TokenResponse;
import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.api.auth.port.TokenIssuer;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.user.validator.UserValidator;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

	@Mock
	UserAdaptor userAdaptor;
	@Mock
	UserValidator userValidator;
	@Mock
	TokenIssuer tokenIssuer;
	@Mock
	RefreshTokenStore refreshTokenStore;
	@InjectMocks
	LoginUseCase useCase;

	@Test
	void unknownIdAndWrongPasswordGiveSameError() {
		given(userAdaptor.findByLoginId("nobody")).willReturn(Optional.empty());
		assertThatThrownBy(() -> useCase.execute(new LoginRequest("nobody", "pw")))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_CREDENTIALS);

		User user = User.builder().id(1L).loginId("driver").passwordHash("hash").build();
		given(userAdaptor.findByLoginId("driver")).willReturn(Optional.of(user));
		willThrow(new GeneralException(ErrorStatus.INVALID_CREDENTIALS))
				.given(userValidator).validatePasswordMatch("wrong", "hash");
		assertThatThrownBy(() -> useCase.execute(new LoginRequest("driver", "wrong")))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_CREDENTIALS);

		verifyNoInteractions(tokenIssuer, refreshTokenStore);
	}

	@Test
	void storesRefreshTokenForItsLifetime() {
		User user = User.builder().id(1L).loginId("driver").passwordHash("hash").build();
		given(userAdaptor.findByLoginId("driver")).willReturn(Optional.of(user));
		given(tokenIssuer.issueAccessToken(1L)).willReturn("access");
		given(tokenIssuer.issueRefreshToken(1L)).willReturn("refresh");
		given(tokenIssuer.refreshTokenTtl()).willReturn(Duration.ofDays(14));

		TokenResponse response = useCase.execute(new LoginRequest("driver", "pw12345678"));

		assertThat(response).isEqualTo(new TokenResponse("access", "refresh"));
		verify(refreshTokenStore).save(1L, "refresh", Duration.ofDays(14));
	}

}
