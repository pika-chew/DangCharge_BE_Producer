package com.picka_chew.dangcharge.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 현재 시각을 Clock으로 주입받아 테스트에서 시각을 고정할 수 있게 한다 */
@Configuration
public class TimeConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}

}
