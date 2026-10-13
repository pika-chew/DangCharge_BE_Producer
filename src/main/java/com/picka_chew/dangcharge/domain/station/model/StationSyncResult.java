package com.picka_chew.dangcharge.domain.station.model;

/**
 * 충전소 동기화 결과.
 *
 * @param created 새로 저장한 충전소 수
 * @param updated 이미 있어 값을 갱신한 충전소 수
 */
public record StationSyncResult(int created, int updated) {
}
