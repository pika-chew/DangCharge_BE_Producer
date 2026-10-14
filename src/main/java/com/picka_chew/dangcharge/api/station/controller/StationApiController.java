package com.picka_chew.dangcharge.api.station.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.station.dto.AvailableSlotResponse;
import com.picka_chew.dangcharge.api.station.dto.ChargerResponse;
import com.picka_chew.dangcharge.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge.api.station.dto.StationSummaryResponse;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;

@RestController
@RequestMapping("/api/v1/stations")
public class StationApiController {

	/** 지도 반경 내 충전소 목록. 마커용 가벼운 응답 */
	@GetMapping
	public ApiResponseDto<List<StationMarkerResponse>> getStationsInRadius(
			@RequestParam BigDecimal lat,
			@RequestParam BigDecimal lng,
			@RequestParam BigDecimal radius,
			@RequestParam(required = false) String connectorType,
			@RequestParam(required = false) BigDecimal minKw,
			@RequestParam(defaultValue = "false") boolean availableOnly) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 주소/이름 검색 */
	@GetMapping("/search")
	public ApiResponseDto<List<StationSummaryResponse>> searchStations(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String region,
			@RequestParam(required = false) String connectorType,
			@RequestParam(required = false) BigDecimal minKw,
			@RequestParam(defaultValue = "false") boolean availableOnly) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전소 상세 */
	@GetMapping("/{stationId}")
	public ApiResponseDto<StationDetailResponse> getStation(@PathVariable Long stationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 충전기별 타입·출력(kW)·현재 상태 */
	@GetMapping("/{stationId}/chargers")
	public ApiResponseDto<List<ChargerResponse>> getChargers(@PathVariable Long stationId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

	/** 날짜별 예약 가능 시간 슬롯. 내 차량과 호환되는 충전기만 내려준다. 로그인 필요 */
	@GetMapping("/{stationId}/availability")
	public ApiResponseDto<List<AvailableSlotResponse>> getAvailability(
			@PathVariable Long stationId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) Long vehicleId) {
		throw new GeneralException(ErrorStatus.NOT_IMPLEMENTED);
	}

}
