package com.picka_chew.dangcharge.infra.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

	/**
	 * 보내지 않은 이벤트를 오래된 순으로 잠그며 가져온다. 다른 파드가 잠근 행은 건너뛰어
	 * 레플리카가 여럿이어도 같은 이벤트를 두 번 보내지 않는다.
	 */
	@NativeQuery("select * from outbox_events where sent_at is null order by id limit :limit for update skip locked")
	List<OutboxEvent> lockUnsent(@Param("limit") int limit);

}
