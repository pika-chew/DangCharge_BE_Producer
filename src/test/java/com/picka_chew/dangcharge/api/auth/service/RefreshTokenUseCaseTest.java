package com.picka_chew.dangcharge.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.picka_chew.dangcharge.api.auth.port.RefreshTokenStore;
import com.picka_chew.dangcharge.api.auth.port.TokenIssuer;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

	@Mock
	TokenIssuer tokenIssuer;
	@Mock
	RefreshTokenStore refreshTokenStore;
	@Mock
	UserAdaptor userAdaptor;
	@InjectMocks
	RefreshTokenUseCase useCase;

	@Test
	void issuesAccessTokenWhenStoredTokenMatches() {
		given(tokenIssuer.verifyRefreshToken("refresh")).willReturn(1L);
		given(refreshTokenStore.find(1L)).willReturn(Optional.of("refresh"));
		given(tokenIssuer.issueAccessToken(1L)).willReturn("access");

		assertThat(useCase.execute("refresh").accessToken()).isEqualTo("access");
		verify(refreshTokenStore, never()).delete(1L);
	}

	@Test
	void mismatchIsTreatedAsTheftAndRevokesStoredToken() {
		given(tokenIssuer.verifyRefreshToken("old")).willReturn(1L);
		given(refreshTokenStore.find(1L)).willReturn(Optional.of("current"));

		assertThatThrownBy(() -> useCase.execute("old"))
				.extracting("errorStatus").isEqualTo(ErrorStatus.INVALID_REFRESH_TOKEN);
		verify(refreshTokenStore).delete(1L);
	}

}
