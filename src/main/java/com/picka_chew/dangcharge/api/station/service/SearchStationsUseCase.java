package com.picka_chew.dangcharge.api.station.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.StationSummaryResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge.domain.station.policy.MetroNamePolicy;

import lombok.RequiredArgsConstructor;

/** 검색어(이름·주소 부분 일치), 시도, 시군구 중 하나 이상이 있어야 한다. 시도는 "서울시"처럼 줄여 써도 된다. 최대 50건 */
@UseCase
@RequiredArgsConstructor
public class SearchStationsUseCase {

	private static final int MAX_RESULTS = 50;

	private final StationAdaptor stationAdaptor;

	@Transactional(readOnly = true)
	public List<StationSummaryResponse> execute(String keyword, String metro, String city) {
		String trimmedKeyword = blankToNull(keyword);
		String trimmedMetro = MetroNamePolicy.toOfficialName(blankToNull(metro));
		String trimmedCity = blankToNull(city);
		if (trimmedKeyword == null && trimmedMetro == null && trimmedCity == null) {
			throw new GeneralException(ErrorStatus.INVALID_SEARCH_CONDITION);
		}
		return stationAdaptor.search(trimmedKeyword, trimmedMetro, trimmedCity, MAX_RESULTS).stream()
				.map(StationSummaryResponse::from)
				.toList();
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.strip();
	}

}
