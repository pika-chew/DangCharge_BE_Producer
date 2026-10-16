package com.picka_chew.dangcharge.api.station.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.station.dto.AvailableSlotResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;
import com.picka_chew.dangcharge.domain.reservation.entity.Reservation;
import com.picka_chew.dangcharge.domain.reservation.policy.ReservationTimePolicy;
import com.picka_chew.dangcharge.domain.station.adaptor.ChargerAdaptor;
import com.picka_chew.dangcharge.domain.station.adaptor.StationAdaptor;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.user.adaptor.UserAdaptor;
import com.picka_chew.dangcharge.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

/**
 * 날짜별 예약 시간대(30분 간격 시작, 1시간 이용). 이미 예약된 시간대도 status=disable로 함께 내려준다. 로그인 사용자의 차종을 충전소가 지원하지 않으면 빈 목록이다.
 * 여기서 비어 있다고 나와도 최종 판단은 예약 생성에서 다시 한다.
 */
@UseCase
@RequiredArgsConstructor
public class GetAvailabilityUseCase {

	private final StationAdaptor stationAdaptor;
	private final ChargerAdaptor chargerAdaptor;
	private final ReservationAdaptor reservationAdaptor;
	private final UserAdaptor userAdaptor;
	private final Clock clock;

	@Transactional(readOnly = true)
	public List<AvailableSlotResponse> execute(Long userId, Long stationId, LocalDate date) {
		Instant now = Instant.now(clock);
		if (!ReservationTimePolicy.isBookableDate(date, now)) {
			throw new GeneralException(ErrorStatus.INVALID_AVAILABILITY_DATE);
		}
		Station station = stationAdaptor.getById(stationId);
		User user = userAdaptor.getById(userId);
		if (!station.supports(user.getVehicleModel())) {
			return List.of();
		}

		List<Charger> chargers = chargerAdaptor.findAllByStationId(stationId);
		List<Reservation> reservations = reservationAdaptor.findConfirmedOverlapping(
				chargers.stream().map(Charger::getId).toList(),
				ReservationTimePolicy.startOfDay(date),
				ReservationTimePolicy.endOf(ReservationTimePolicy.startOfDay(date.plusDays(1))));
		return ReservationTimePolicy.slotsOf(chargers, reservations, date, now).stream()
				.map(AvailableSlotResponse::from)
				.toList();
	}

}
