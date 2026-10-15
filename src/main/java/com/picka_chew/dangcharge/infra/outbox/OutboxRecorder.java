package com.picka_chew.dangcharge.infra.outbox;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.picka_chew.dangcharge.common.event.DomainEvent;
import com.picka_chew.dangcharge.common.event.DomainEventRecorder;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.json.JsonMapper;

@Component
@RequiredArgsConstructor
public class OutboxRecorder implements DomainEventRecorder {

	private final OutboxEventRepository outboxEventRepository;
	private final JsonMapper jsonMapper;
	private final Clock clock;

	/** 비즈니스 데이터와 같은 트랜잭션에서만 기록한다. 트랜잭션 밖에서 부르면 예외다 */
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void record(DomainEvent event) {
		outboxEventRepository.save(new OutboxEvent(event.aggregateType(), event.aggregateId(), event.eventType(),
				jsonMapper.writeValueAsString(event), Instant.now(clock)));
	}

}
