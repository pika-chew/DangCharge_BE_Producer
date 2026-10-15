package com.picka_chew.dangcharge.api.common.dto;

import com.picka_chew.dangcharge.common.exception.ErrorStatus;

/**
 * 공통 응답 포맷. 성공하면 data만, 실패하면 error만 채운다.
 */
public record ApiResponseDto<T>(boolean success, T data, Error error) {

	public static <T> ApiResponseDto<T> onSuccess(T data) {
		return new ApiResponseDto<>(true, data, null);
	}

	public static ApiResponseDto<Void> onFailure(ErrorStatus errorStatus) {
		return onFailure(errorStatus.getCode(), errorStatus.getMessage());
	}

	public static ApiResponseDto<Void> onFailure(String code, String message) {
		return new ApiResponseDto<>(false, null, new Error(code, message));
	}

	public record Error(String code, String message) {
	}

}
