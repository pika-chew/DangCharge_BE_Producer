package com.picka_chew.dangcharge.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorStatus {

	// 공통
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다"),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그인이 필요합니다"),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다"),
	CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "요청이 현재 데이터와 충돌합니다"),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 에러, 관리자에게 문의 바랍니다"),

	// 인증·사용자
	DUPLICATED_LOGIN_ID(HttpStatus.CONFLICT, "DUPLICATED_LOGIN_ID", "이미 사용 중인 아이디입니다"),
	INVALID_PASSWORD_POLICY(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_POLICY", "비밀번호는 8~64자이며 영문과 숫자를 모두 포함해야 합니다"),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다"),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다"),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다"),
	VEHICLE_IN_USE(HttpStatus.CONFLICT, "VEHICLE_IN_USE", "진행 중인 예약이 있어 차종을 바꿀 수 없습니다"),

	// 충전소
	STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "STATION_NOT_FOUND", "존재하지 않는 충전소입니다"),
	CHARGER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHARGER_NOT_FOUND", "존재하지 않는 충전기입니다"),
	INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "INVALID_SEARCH_CONDITION", "검색어, 시도, 시군구 중 하나 이상을 입력해야 합니다"),
	INVALID_RADIUS(HttpStatus.BAD_REQUEST, "INVALID_RADIUS", "반경은 0보다 크고 20km 이하여야 합니다"),
	INVALID_AVAILABILITY_DATE(HttpStatus.BAD_REQUEST, "INVALID_AVAILABILITY_DATE", "오늘부터 예약 가능 기간 안의 날짜만 조회할 수 있습니다"),

	// 예약·충전
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RESERVATION_NOT_FOUND", "존재하지 않는 예약입니다"),
	RESERVATION_CONFLICT(HttpStatus.CONFLICT, "RESERVATION_CONFLICT", "이미 예약된 시간입니다"),
	RESERVATION_ALREADY_ACTIVE(HttpStatus.CONFLICT, "RESERVATION_ALREADY_ACTIVE", "진행 중인 예약이 이미 있습니다"),
	VEHICLE_NOT_COMPATIBLE(HttpStatus.UNPROCESSABLE_CONTENT, "VEHICLE_NOT_COMPATIBLE", "이 충전소에서 충전할 수 없는 차종입니다"),
	CHARGER_UNAVAILABLE(HttpStatus.CONFLICT, "CHARGER_UNAVAILABLE", "고장 또는 점검 중인 충전기입니다"),
	INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "INVALID_RESERVATION_TIME", "예약 시간이 올바르지 않습니다"),
	RESERVATION_NOT_CANCELABLE(HttpStatus.CONFLICT, "RESERVATION_NOT_CANCELABLE", "시작 10분 전까지만 취소할 수 있습니다"),
	CHECK_IN_NOT_ALLOWED(HttpStatus.CONFLICT, "CHECK_IN_NOT_ALLOWED", "예약 시작 10분 전부터 시작 15분 후까지만 충전을 시작할 수 있습니다"),
	INVALID_RESERVATION_STATUS(HttpStatus.CONFLICT, "INVALID_RESERVATION_STATUS", "현재 예약 상태에서는 할 수 없는 요청입니다"),
	DUPLICATED_REQUEST(HttpStatus.CONFLICT, "DUPLICATED_REQUEST", "같은 요청을 처리하고 있습니다"),
	CHARGING_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CHARGING_HISTORY_NOT_FOUND", "존재하지 않는 충전 내역입니다");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
