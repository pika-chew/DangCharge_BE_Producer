package com.picka_chew.dangcharge.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

	// 공통
	NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "NOT_IMPLEMENTED", "아직 구현되지 않은 API입니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
