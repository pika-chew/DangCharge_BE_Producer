package com.picka_chew.dangcharge.api.charging.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge.api.common.dto.PagedResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

/** 충전을 마친 예약을 최근 완료 순으로 */
@UseCase
@RequiredArgsConstructor
public class GetChargingHistoriesUseCase {

	private final ReservationAdaptor reservationAdaptor;

	@Transactional(readOnly = true)
	public PagedResponse<ChargingHistoryResponse> execute(Long userId, int page, int size) {
		return PagedResponse.of(reservationAdaptor.findCompletedByUserId(userId, PageRequest.of(page, size)),
				ChargingHistoryResponse::from);
	}

}
