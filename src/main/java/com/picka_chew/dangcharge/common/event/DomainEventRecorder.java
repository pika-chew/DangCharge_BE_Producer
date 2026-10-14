package com.picka_chew.dangcharge.common.event;

/**
 * 도메인 이벤트를 비즈니스 데이터와 같은 트랜잭션에 기록한다. 실제 발행은 커밋 이후 따로 한다.
 * 구현체: infra/outbox/OutboxRecorder
 */
public interface DomainEventRecorder {

	void record(DomainEvent event);

}
