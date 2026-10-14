package com.picka_chew.dangcharge.domain.vehicle.entity;

import java.util.Arrays;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자가 고를 수 있는 차종. 한전 충전소 설치현황의 지원 차종(carType)과 같은 목록이라
 * 충전소가 지원하는 차종과 그대로 비교할 수 있다.
 */
@Getter
@RequiredArgsConstructor
public enum VehicleModel {

	SM3_ZE("SM3 Z.E"),
	RAY_EV("레이EV"),
	SOUL_EV("소울EV"),
	NISSAN_LEAF("닛산리프"),
	IONIQ_EV("아이오닉EV"),
	BMW_I3("BMW i3"),
	SPARK_EV("스파크EV"),
	BOLT_EV("볼트EV"),
	TESLA("테슬라");

	/** 한전 설치현황에 적힌 차종 이름. 화면에 보여줄 이름으로도 쓴다 */
	private final String label;

	public static Optional<VehicleModel> fromLabel(String label) {
		if (label == null) {
			return Optional.empty();
		}
		String trimmed = label.trim();
		return Arrays.stream(values())
				.filter(model -> model.label.equals(trimmed))
				.findFirst();
	}

}
