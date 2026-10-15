package com.picka_chew.dangcharge.infra.outbox;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * 보내지 않은 outbox 이벤트를 짧은 주기로 Kafka에 보낸다. 토픽은 dangcharge.{aggregateType}, key는 aggregateId다.
 * 메시지에는 consumer가 오래된 이벤트를 걸러낼 수 있게 version(= outbox id, 단조 증가)을 담는다.
 * 전송에 실패하면 그 뒤 이벤트는 보내지 않고 다음 주기에 다시 시도해 순서를 지킨다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dangcharge.outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxRelay {

	static final String TOPIC_PREFIX = "dangcharge.";
	private static final int BATCH_SIZE = 100;
	private static final long SEND_TIMEOUT_SECONDS = 10;

	private final OutboxEventRepository outboxEventRepository;
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final TransactionTemplate transactionTemplate;
	private final JsonMapper jsonMapper;
	private final Clock clock;

	@Scheduled(fixedDelayString = "${dangcharge.outbox.relay.interval:1000}")
	public void relay() {
		transactionTemplate.executeWithoutResult(status -> {
			List<OutboxEvent> events = outboxEventRepository.lockUnsent(BATCH_SIZE);
			for (OutboxEvent event : events) {
				if (!send(event)) {
					return;
				}
				event.markSent(Instant.now(clock));
			}
		});
	}

	private boolean send(OutboxEvent event) {
		try {
			kafkaTemplate.send(TOPIC_PREFIX + event.getAggregateType(), String.valueOf(event.getAggregateId()),
					toMessage(event)).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return true;
		}
		catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			return false;
		}
		catch (Exception exception) {
			log.warn("outbox 이벤트 전송 실패, 다음 주기에 다시 보냄: id={}, type={}", event.getId(),
					event.getEventType(), exception);
			return false;
		}
	}

	String toMessage(OutboxEvent event) {
		Map<String, Object> message = new LinkedHashMap<>();
		message.put("eventId", event.getId());
		message.put("eventType", event.getEventType());
		message.put("aggregateType", event.getAggregateType());
		message.put("aggregateId", event.getAggregateId());
		message.put("version", event.getId());
		message.put("occurredAt", event.getOccurredAt());
		message.put("payload", jsonMapper.readTree(event.getPayload()));
		return jsonMapper.writeValueAsString(message);
	}

}
