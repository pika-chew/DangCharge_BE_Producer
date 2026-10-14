package com.picka_chew.dangcharge.infra.idempotency;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 요청의 Idempotency-Key와 그 키로 만든 예약. 하루가 지나면 지운다 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation_idempotency_keys",
		uniqueConstraints = @UniqueConstraint(name = "uq_reservation_idempotency_user_key",
				columnNames = {"user_id", "idempotency_key"}),
		indexes = @Index(name = "idx_reservation_idempotency_created", columnList = "created_at"))
public class ReservationIdempotencyKey {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "idempotency_key", nullable = false, length = 100)
	private String idempotencyKey;

	@Column(name = "reservation_id", nullable = false)
	private Long reservationId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	ReservationIdempotencyKey(Long userId, String idempotencyKey, Long reservationId, Instant createdAt) {
		this.userId = userId;
		this.idempotencyKey = idempotencyKey;
		this.reservationId = reservationId;
		this.createdAt = createdAt;
	}

}
