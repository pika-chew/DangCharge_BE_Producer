package com.picka_chew.dangcharge.api.common.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.swagger.v3.oas.annotations.Hidden;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@Hidden
@RestControllerAdvice
public class ExceptionAdvice {

	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(GeneralException exception) {
		ErrorStatus errorStatus = exception.getErrorStatus();
		return ResponseEntity.status(errorStatus.getHttpStatus()).body(ApiResponseDto.onFailure(errorStatus));
	}

}
