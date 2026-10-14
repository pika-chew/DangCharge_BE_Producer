package com.picka_chew.dangcharge.domain.reservation.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.picka_chew.dangcharge.domain.common.entity.BaseTimeEntity;
import com.picka_chew.dangcharge.domain.station.entity.Charger;
import com.picka_chew.dangcharge.common.exception.ErrorStatus;
import com.picka_chew.dangcharge.common.exception.GeneralException;
import com.picka_chew.dangcharge.domain.user.entity.User;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 충전구 예약. 아래 조건은 DB 제약으로 막지 못하므로 예약을 만들거나 시간을 바꾸는 쪽에서 보장해야 한다.
 * <ul>
 * <li>같은 충전구의 CONFIRMED 예약끼리 시간이 겹치지 않는다. 동시 요청도 막아야 한다.</li>
 * <li>예약한 사용자의 차종을 충전소가 지원하고, 충전구가 운영 중이다.</li>
 * </ul>
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "reservations", indexes = {
		@Index(name = "idx_reservations_user_starts", columnList = "user_id, starts_at"),
		@Index(name = "idx_reservations_charger_status_time", columnList = "charger_id, status, starts_at, ends_at")
})
public class Reservation extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservations_user"))
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "charger_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservations_charger"))
	private Charger charger;

	/** 예약 시작(UTC) */
	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	/** 예약 종료(UTC). 시작보다 뒤여야 한다 */
	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private ReservationStatus status = ReservationStatus.CONFIRMED;

	/** 예약 당시 예상 요금(원). 없으면 null, 있으면 0 이상 */
	@Column(name = "estimated_fee", precision = 12, scale = 2)
	private BigDecimal estimatedFee;

	/** 예약 당시 사용자의 차종. 사용자가 나중에 차종을 바꿔도 충전 내역에는 이 값을 보여준다 */
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "vehicle_model", length = 30)
	private VehicleModel vehicleModel;

	/** 충전 완료(COMPLETED) 시각(UTC) */
	@Column(name = "completed_at")
	private Instant completedAt;

	public void cancel() {
		requireConfirmed();
		this.status = ReservationStatus.CANCELLED;
	}

	public void complete(Instant completedAt) {
		requireConfirmed();
		this.status = ReservationStatus.COMPLETED;
		this.completedAt = completedAt;
	}

	private void requireConfirmed() {
		if (status != ReservationStatus.CONFIRMED) {
			throw new GeneralException(ErrorStatus.INVALID_RESERVATION_STATUS);
		}
	}

}
