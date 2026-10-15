package com.picka_chew.dangcharge.infra.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class OutboxRelayTest {

	private static final Instant NOW = Instant.parse("2026-10-14T01:00:00Z");

	OutboxEventRepository repository = mock(OutboxEventRepository.class);
	@SuppressWarnings("unchecked")
	KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
	JsonMapper jsonMapper = JsonMapper.builder().build();
	OutboxRelay relay;

	@BeforeEach
	void setUp() {
		relay = new OutboxRelay(repository, kafkaTemplate,
				new TransactionTemplate(mock(PlatformTransactionManager.class)), jsonMapper,
				Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void messageCarriesVersionAndSnapshot() {
		OutboxEvent event = event(42L, "reservation", 7L, "{\"reservationId\":7,\"status\":\"CONFIRMED\"}");

		JsonNode message = jsonMapper.readTree(relay.toMessage(event));

		assertThat(message.get("version").asLong()).isEqualTo(42L);
		assertThat(message.get("eventType").asString()).isEqualTo("ReservationCreated");
		assertThat(message.get("payload").get("status").asString()).isEqualTo("CONFIRMED");
	}

	@Test
	void sendsByAggregateKeyAndStopsAtFirstFailureToKeepOrder() {
		OutboxEvent first = event(1L, "reservation", 7L, "{}");
		OutboxEvent second = event(2L, "reservation", 7L, "{}");
		OutboxEvent third = event(3L, "user", 9L, "{}");
		given(repository.lockUnsent(100)).willReturn(List.of(first, second, third));
		given(kafkaTemplate.send(eq("dangcharge.reservation"), eq("7"), anyString()))
				.willReturn(CompletableFuture.completedFuture(mock(SendResult.class)))
				.willReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));

		relay.relay();

		assertThat(first.getSentAt()).isEqualTo(NOW);
		assertThat(second.getSentAt()).isNull();
		assertThat(third.getSentAt()).isNull();
		verify(kafkaTemplate, never()).send(eq("dangcharge.user"), anyString(), anyString());
	}

	private static OutboxEvent event(Long id, String aggregateType, Long aggregateId, String payload) {
		OutboxEvent event = new OutboxEvent(aggregateType, aggregateId, "ReservationCreated", payload, NOW);
		ReflectionTestUtils.setField(event, "id", id);
		return event;
	}

}
