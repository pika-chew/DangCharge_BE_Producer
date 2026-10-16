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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.picka_chew.dangcharge.api.common.dto.ApiResponseDto;
import com.picka_chew.dangcharge.api.station.dto.AvailableSlotResponse;
import com.picka_chew.dangcharge.api.station.dto.ChargerResponse;
import com.picka_chew.dangcharge.api.station.dto.StationDetailResponse;
import com.picka_chew.dangcharge.api.station.dto.StationMarkerResponse;
import com.picka_chew.dangcharge.api.station.dto.StationSummaryResponse;
import com.picka_chew.dangcharge.api.station.service.GetAvailabilityUseCase;
import com.picka_chew.dangcharge.api.station.service.GetChargersUseCase;
import com.picka_chew.dangcharge.api.station.service.GetNearbyStationsUseCase;
import com.picka_chew.dangcharge.api.station.service.GetStationUseCase;
import com.picka_chew.dangcharge.api.station.service.SearchStationsUseCase;
import com.picka_chew.dangcharge.common.annotation.CurrentUser;
import com.picka_chew.dangcharge.security.dto.AuthUser;

import lombok.RequiredArgsConstructor;

@Tag(name = "station", description = "[충전소] 충전소 조회·검색")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stations")
public class StationApiController {

	private final GetNearbyStationsUseCase getNearbyStationsUseCase;
	private final SearchStationsUseCase searchStationsUseCase;
	private final GetStationUseCase getStationUseCase;
	private final GetChargersUseCase getChargersUseCase;
	private final GetAvailabilityUseCase getAvailabilityUseCase;

	@Operation(summary = "지도 반경 내 충전소 목록",
			description = "마커용 가벼운 응답. radius는 미터 단위이고 최대 20km, 가까운 순으로 최대 200건이다. "
					+ "좌표가 없는 충전소는 나오지 않는다.")
	@SecurityRequirements
	@GetMapping
	public ApiResponseDto<List<StationMarkerResponse>> getStationsInRadius(
			@RequestParam BigDecimal lat,
			@RequestParam BigDecimal lng,
			@RequestParam BigDecimal radius,
			@RequestParam(required = false) BigDecimal minKw,
			@RequestParam(defaultValue = "false") boolean availableOnly) {
		return ApiResponseDto.onSuccess(
				getNearbyStationsUseCase.execute(lat, lng, radius, minKw, availableOnly));
	}

	@Operation(summary = "주소/이름 검색",
			description = "시도(metro)와 시군구(city)로 범위를 좁힐 수 있다. 시도는 \"서울\", \"서울시\"처럼 줄여 써도 된다. "
					+ "시군구는 \"강남구\"처럼 정식 이름으로 넣는다. 세 조건 중 하나 이상이 필요하고 최대 50건이다.")
	@SecurityRequirements
	@GetMapping("/search")
	public ApiResponseDto<List<StationSummaryResponse>> searchStations(
			@Parameter(description = "충전소 이름·주소에 들어간 글자", example = "강남구청")
			@RequestParam(required = false) String keyword,
			@Parameter(description = "시도. 줄여 써도 된다", example = "서울특별시")
			@RequestParam(required = false) String metro,
			@Parameter(description = "시군구", example = "강남구")
			@RequestParam(required = false) String city) {
		return ApiResponseDto.onSuccess(searchStationsUseCase.execute(keyword, metro, city));
	}

	@Operation(summary = "충전소 상세", description = "충전소 정보와 소속 충전기 전체(타입·출력·상태)를 함께 내려준다.")
	@SecurityRequirements
	@GetMapping("/{stationId}")
	public ApiResponseDto<StationDetailResponse> getStation(@PathVariable Long stationId) {
		return ApiResponseDto.onSuccess(getStationUseCase.execute(stationId));
	}

	@Operation(summary = "충전기별 타입·출력(kW)·현재 상태")
	@SecurityRequirements
	@GetMapping("/{stationId}/chargers")
	public ApiResponseDto<List<ChargerResponse>> getChargers(@PathVariable Long stationId) {
		return ApiResponseDto.onSuccess(getChargersUseCase.execute(stationId));
	}

	@Operation(summary = "날짜별 예약 가능 시간 슬롯",
			description = "충전기별로 1시간 예약 구간을 30분 간격 시작 시각으로 내려준다. "
					+ "status가 enable이면 예약할 수 있고, disable이면 이미 예약된(예약·충전중) 시간대다. "
					+ "지난 시작 시각과 고장·점검 중인 충전기는 빠진다. 내 차종을 지원하지 않는 충전소면 빈 목록이다. "
					+ "로그인이 필요하다.")
	@GetMapping("/{stationId}/availability")
	public ApiResponseDto<List<AvailableSlotResponse>> getAvailability(
			@CurrentUser AuthUser authUser,
			@PathVariable Long stationId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
		return ApiResponseDto.onSuccess(getAvailabilityUseCase.execute(authUser.userId(), stationId, date));
	}

}
