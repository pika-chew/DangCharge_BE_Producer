package com.picka_chew.dangcharge.infra.outbox;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아직 Kafka로 보내지 않았거나 보낸 도메인 이벤트. 비즈니스 데이터와 같은 트랜잭션에서 저장해
 * DB 롤백인데 이벤트만 나가거나, 그 반대가 되는 일을 막는다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_unsent", columnList = "sent_at, id"))
public class OutboxEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "aggregate_type", nullable = false, length = 50)
	private String aggregateType;

	@Column(name = "aggregate_id", nullable = false)
	private Long aggregateId;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	/** 이벤트 스냅샷 JSON */
	@Lob
	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	/** Kafka 전송 완료 시각. 없으면 아직 보내지 않은 이벤트 */
	@Column(name = "sent_at")
	private Instant sentAt;

	public OutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload, Instant occurredAt) {
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
		this.occurredAt = occurredAt;
	}

	public void markSent(Instant sentAt) {
		this.sentAt = sentAt;
	}

}
