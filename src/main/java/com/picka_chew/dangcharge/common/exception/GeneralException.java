package com.picka_chew.dangcharge.common.exception;

import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

	private final ErrorStatus errorStatus;

	public GeneralException(ErrorStatus errorStatus) {
		super(errorStatus.getMessage());
		this.errorStatus = errorStatus;
	}

}
