package com.picka_chew.dangcharge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// 환경 변수와 상관없이 기동 시 CSV 적재와 Kafka 전송을 끈다
@SpringBootTest(properties = {"dangcharge.station-import.enabled=false", "dangcharge.outbox.relay.enabled=false",
		"dangcharge.no-show.enabled=false"})
class DangchargeApplicationTests {

	@Test
	void contextLoads() {
	}

}
