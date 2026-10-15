package com.picka_chew.dangcharge.domain.reservation.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * 예약 상태. CONFIRMED → CHARGING → COMPLETED 순서로 진행하고, CONFIRMED에서만 취소할 수 있다.
 * 진행 중인 예약은 CONFIRMED(예약)와 CHARGING(충전중)이다.
 */
public enum ReservationStatus {

	CONFIRMED,
	CHARGING,
	CANCELLED,
	COMPLETED,
	NO_SHOW;

	/** 진행 중인 예약 상태. 같은 충전기 시간 겹침을 막을 때 쓴다 */
	public static final Set<ReservationStatus> IN_PROGRESS = EnumSet.of(CONFIRMED, CHARGING);

}
