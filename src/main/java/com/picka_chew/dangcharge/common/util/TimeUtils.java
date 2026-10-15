package com.picka_chew.dangcharge.common.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/** 서버는 UTC로 저장하고, API 응답과 날짜 경계는 한국 시간으로 다룬다 */
public final class TimeUtils {

	public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

	private TimeUtils() {
	}

	public static OffsetDateTime toOffsetDateTime(Instant instant) {
		return instant == null ? null : instant.atZone(ZONE).toOffsetDateTime();
	}

}
