package com.picka_chew.dangcharge.api.charging.service;

import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.api.charging.dto.ChargingHistoryResponse;
import com.picka_chew.dangcharge.common.annotation.UseCase;
import com.picka_chew.dangcharge.domain.reservation.adaptor.ReservationAdaptor;

import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetChargingHistoryUseCase {

	private final ReservationAdaptor reservationAdaptor;

	@Transactional(readOnly = true)
	public ChargingHistoryResponse execute(Long userId, Long reservationId) {
		return ChargingHistoryResponse.from(reservationAdaptor.getCompletedByIdAndUserId(reservationId, userId));
	}

}
