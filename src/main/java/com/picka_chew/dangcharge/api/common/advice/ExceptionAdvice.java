package com.picka_chew.dangcharge.api.common.advice;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.swagger.v3.oas.annotations.Hidden;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

import lombok.extern.slf4j.Slf4j;

/**
 * 모든 오류를 공통 응답 포맷으로 바꾼다. 스프링 MVC 기본 예외(파라미터 누락, 타입 불일치, 본문 파싱 실패 등)는
 * ResponseEntityExceptionHandler가 상태 코드를 정하고, 본문만 공통 포맷으로 감싼다.
 */
@Slf4j
@Hidden
@RestControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

	@ExceptionHandler(GeneralException.class)
	public ResponseEntity<ApiResponseDto<Void>> handleGeneralException(GeneralException exception) {
		return toResponse(exception.getErrorStatus());
	}

	/** 동시 요청이 검증을 함께 통과해 DB 제약에 걸린 경우 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiResponseDto<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		String message = String.valueOf(exception.getMostSpecificCause().getMessage());
		if (message.contains("uq_users_login_id")) {
			return toResponse(ErrorStatus.DUPLICATED_LOGIN_ID);
		}
		log.warn("DB 제약 위반: {}", message);
		return toResponse(ErrorStatus.CONFLICT);
	}

	/** 락 대기 시간(innodb_lock_wait_timeout)을 넘겼거나 데드락으로 롤백된 경우. 다시 시도하면 성공할 수 있다 */
	@ExceptionHandler(PessimisticLockingFailureException.class)
	public ResponseEntity<ApiResponseDto<Void>> handlePessimisticLockingFailure(
			PessimisticLockingFailureException exception) {
		log.warn("락 획득 실패: {}", exception.getMostSpecificCause().getMessage());
		return toResponse(ErrorStatus.SERVICE_BUSY);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponseDto<Void>> handleUnexpected(Exception exception) {
		log.error("처리하지 못한 예외", exception);
		return toResponse(ErrorStatus.INTERNAL_SERVER_ERROR);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		FieldError fieldError = exception.getBindingResult().getFieldError();
		String message = fieldError == null
				? ErrorStatus.BAD_REQUEST.getMessage()
				: fieldError.getField() + ": " + fieldError.getDefaultMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(ApiResponseDto.onFailure(ErrorStatus.BAD_REQUEST.getCode(), message));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		HttpStatus status = HttpStatus.resolve(statusCode.value());
		String code = status == null ? String.valueOf(statusCode.value()) : status.name();
		String message = status == null ? exception.getMessage() : status.getReasonPhrase();
		return ResponseEntity.status(statusCode).headers(headers).body(ApiResponseDto.onFailure(code, message));
	}

	private static ResponseEntity<ApiResponseDto<Void>> toResponse(ErrorStatus errorStatus) {
		return ResponseEntity.status(errorStatus.getHttpStatus()).body(ApiResponseDto.onFailure(errorStatus));
	}

}
