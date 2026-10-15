package com.picka_chew.dangcharge.domain.station.model;

import java.util.List;

import com.picka_chew.dangcharge.domain.station.entity.Station;

/**
 * 충전소 동기화 결과.
 *
 * @param created 새로 저장한 충전소 수
 * @param updated 이미 있던 충전소 중 값이 바뀐 수. 값이 그대로인 충전소는 세지 않는다
 * @param changedStations 새로 저장했거나 값이나 가상 충전기가 바뀐 충전소. 이벤트를 이 목록만큼 낸다
 */
public record StationSyncResult(int created, int updated, List<Station> changedStations) {
}
