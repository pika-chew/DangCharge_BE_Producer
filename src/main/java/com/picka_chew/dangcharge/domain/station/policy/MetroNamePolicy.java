package com.picka_chew.dangcharge.domain.station.policy;

import java.util.Map;

/**
 * 사용자가 줄여 쓴 시도 이름("서울시", "경기")을 충전소 데이터에 저장된 정식 이름("서울특별시", "경기도")으로 바꾼다.
 * 충전소 시도 값은 한전 설치현황의 표기를 따른다.
 */
public final class MetroNamePolicy {

	private static final Map<String, String> OFFICIAL_NAMES = Map.ofEntries(
			Map.entry("서울", "서울특별시"),
			Map.entry("부산", "부산광역시"),
			Map.entry("대구", "대구광역시"),
			Map.entry("인천", "인천광역시"),
			Map.entry("광주", "광주광역시"),
			Map.entry("대전", "대전광역시"),
			Map.entry("울산", "울산광역시"),
			Map.entry("세종", "세종특별자치시"),
			Map.entry("경기", "경기도"),
			Map.entry("강원", "강원도"),
			Map.entry("충북", "충청북도"),
			Map.entry("충남", "충청남도"),
			Map.entry("전북", "전라북도"),
			Map.entry("전남", "전라남도"),
			Map.entry("경북", "경상북도"),
			Map.entry("경남", "경상남도"),
			Map.entry("제주", "제주특별자치도"));

	private MetroNamePolicy() {
	}

	/** "서울", "서울시", "서울특별시" 모두 "서울특별시"가 된다. 모르는 이름은 그대로 둔다 */
	public static String toOfficialName(String metro) {
		if (metro == null) {
			return null;
		}
		String name = metro.strip();
		String shortName = name.endsWith("시") && name.length() == 3 ? name.substring(0, 2) : name;
		return OFFICIAL_NAMES.getOrDefault(shortName, name);
	}

}
