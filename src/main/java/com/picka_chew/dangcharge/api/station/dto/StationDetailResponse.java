package com.picka_chew.dangcharge.api.station.dto;

public record StationDetailResponse(Long stationId, String name, String address, String usageRestriction, String fee, String parkingInfo) {
}
