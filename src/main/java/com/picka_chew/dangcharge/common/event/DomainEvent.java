package com.picka_chew.dangcharge.common.event;

/**
 * 다른 서비스(consumer)로 내보내는 도메인 이벤트. 이벤트 본문은 id만이 아니라 화면에 필요한 전체 스냅샷을 담는다.
 */
public interface DomainEvent {

	/** 토픽을 나누는 단위. 예: user, station, reservation */
	String aggregateType();

	/** 메시지 key. 같은 애그리거트의 이벤트 순서를 보장하는 기준이다 */
	Long aggregateId();

	String eventType();

}
