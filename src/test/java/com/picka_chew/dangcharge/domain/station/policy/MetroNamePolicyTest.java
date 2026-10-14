package com.picka_chew.dangcharge.domain.station.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MetroNamePolicyTest {

	@ParameterizedTest
	@CsvSource({
			"서울, 서울특별시",
			"서울시, 서울특별시",
			"서울특별시, 서울특별시",
			"부산시, 부산광역시",
			"경기, 경기도",
			"경기도, 경기도",
			"제주, 제주특별자치도",
			"' 서울시 ', 서울특별시",
			"화성시, 화성시"
	})
	void convertsShortNamesToOfficialNames(String input, String expected) {
		assertThat(MetroNamePolicy.toOfficialName(input)).isEqualTo(expected);
	}

}
