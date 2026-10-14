package com.picka_chew.dangcharge.api.station.port;

import java.util.List;

import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;

/**
 * 외부 충전소 설치현황을 읽어오는 포트. 유스케이스가 파일 형식이나 공급원에 의존하지 않도록 분리한다.
 * 구현체: infra/kepco/KepcoStationCsvReader
 */
public interface StationDataReader {

	List<StationSnapshot> read();

}
