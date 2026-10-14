-- 전기차 충전소 예약 서비스 스키마
-- 대상: MySQL 8.0.16 이상 (CHECK 제약 적용).
-- 사용할 데이터베이스를 먼저 선택한 후 실행하세요.
-- charging_sessions는 포함하지 않습니다. 기존 테이블을 삭제하지 않습니다.
-- DATETIME은 시간대를 저장하지 않으므로 애플리케이션에서 UTC로 통일하세요.
SET NAMES utf8mb4;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    login_id VARCHAR(50) NOT NULL COMMENT '로그인 아이디',
    password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
    username VARCHAR(50) NOT NULL COMMENT '유저네임',
    vehicle_model VARCHAR(30) NOT NULL COMMENT '차종, 선택지 중 하나',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_users_login_id UNIQUE (login_id),
    CONSTRAINT chk_users_vehicle_model CHECK (
        vehicle_model IN ('SM3_ZE', 'RAY_EV', 'SOUL_EV', 'NISSAN_LEAF', 'IONIQ_EV', 'BMW_I3', 'SPARK_EV', 'BOLT_EV', 'TESLA')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE stations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(100) NULL COMMENT '외부 ID, 선택; 단일 외부 공급원 기준',
    name VARCHAR(200) NOT NULL COMMENT '충전소 이름',
    address VARCHAR(500) NOT NULL,
    metro VARCHAR(50) NOT NULL COMMENT '시도',
    city VARCHAR(50) NOT NULL COMMENT '시군구',
    rapid_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '급속 충전기 수',
    slow_count INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '완속 충전기 수',
    latitude DECIMAL(10,7) NULL COMMENT '위도, 선택',
    longitude DECIMAL(10,7) NULL COMMENT '경도, 선택',
    synced_at DATETIME(6) NULL COMMENT '마지막 동기화 시각',
    PRIMARY KEY (id),
    KEY idx_stations_metro_city (metro, city),
    CONSTRAINT uq_stations_external UNIQUE (external_id),
    CONSTRAINT uq_stations_name_address UNIQUE (name, address),
    CONSTRAINT chk_stations_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT chk_stations_longitude CHECK (longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 충전소에서 충전할 수 있는 차종. 한전 설치현황의 carType을 차종별 행으로 풀어 저장한다.
CREATE TABLE station_supported_models (
    station_id BIGINT UNSIGNED NOT NULL,
    vehicle_model VARCHAR(30) NOT NULL COMMENT '충전 가능 차종, users.vehicle_model과 같은 값',
    PRIMARY KEY (station_id, vehicle_model),
    KEY idx_station_models_model (vehicle_model),
    CONSTRAINT fk_station_models_station FOREIGN KEY (station_id) REFERENCES stations (id),
    CONSTRAINT chk_station_models_model CHECK (
        vehicle_model IN ('SM3_ZE', 'RAY_EV', 'SOUL_EV', 'NISSAN_LEAF', 'IONIQ_EV', 'BMW_I3', 'SPARK_EV', 'BOLT_EV', 'TESLA')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE chargers (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    station_id BIGINT UNSIGNED NOT NULL,
    external_id VARCHAR(100) NULL COMMENT '충전소 내 외부 ID, 선택',
    charging_type VARCHAR(20) NOT NULL COMMENT 'FAST: 급속, SLOW: 완속',
    max_power_kw DECIMAL(8,2) NULL COMMENT '최대 출력, 선택',
    operational_status VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN'
        COMMENT 'AVAILABLE, IN_USE, OUT_OF_SERVICE, UNKNOWN',
    data_source VARCHAR(20) NOT NULL COMMENT 'REAL 또는 SIMULATION',
    PRIMARY KEY (id),
    CONSTRAINT uq_chargers_external UNIQUE (station_id, external_id),
    CONSTRAINT fk_chargers_station FOREIGN KEY (station_id) REFERENCES stations (id),
    CONSTRAINT chk_chargers_type CHECK (charging_type IN ('FAST', 'SLOW')),
    CONSTRAINT chk_chargers_power CHECK (max_power_kw > 0),
    CONSTRAINT chk_chargers_status CHECK (
        operational_status IN ('AVAILABLE', 'IN_USE', 'OUT_OF_SERVICE', 'UNKNOWN')
    ),
    CONSTRAINT chk_chargers_source CHECK (data_source IN ('REAL', 'SIMULATION'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservations (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    charger_id BIGINT UNSIGNED NOT NULL,
    starts_at DATETIME(6) NOT NULL COMMENT '예약 시작, UTC',
    ends_at DATETIME(6) NOT NULL COMMENT '예약 종료, UTC',
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED'
        COMMENT 'CONFIRMED, CANCELLED, COMPLETED, NO_SHOW',
    estimated_fee DECIMAL(12,2) NULL COMMENT '예약 당시 예상 요금, 원, 선택',
    vehicle_model VARCHAR(30) NULL COMMENT '예약 당시 사용자 차종',
    completed_at DATETIME(6) NULL COMMENT '충전 완료 시각, UTC',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_reservations_user_starts (user_id, starts_at),
    KEY idx_reservations_charger_status_time (charger_id, status, starts_at, ends_at),
    KEY idx_reservations_user_status_completed (user_id, status, completed_at),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_reservations_charger FOREIGN KEY (charger_id) REFERENCES chargers (id),
    CONSTRAINT chk_reservations_time CHECK (ends_at > starts_at),
    CONSTRAINT chk_reservations_fee CHECK (estimated_fee >= 0),
    CONSTRAINT chk_reservations_status CHECK (
        status IN ('CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')
    ),
    CONSTRAINT chk_reservations_vehicle_model CHECK (
        vehicle_model IN ('SM3_ZE', 'RAY_EV', 'SOUL_EV', 'NISSAN_LEAF', 'IONIQ_EV', 'BMW_I3', 'SPARK_EV', 'BOLT_EV', 'TESLA')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 사용자별 리프레시 토큰. 원문 대신 SHA-256 해시를 저장하고, 다시 로그인하면 덮어쓴다.
CREATE TABLE refresh_tokens (
    user_id BIGINT UNSIGNED NOT NULL,
    token_hash CHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 로그아웃으로 막은 액세스 토큰(jti). 토큰 만료 시각이 지나면 애플리케이션이 지운다.
CREATE TABLE revoked_access_tokens (
    token_id VARCHAR(36) NOT NULL COMMENT '액세스 토큰 jti',
    expires_at DATETIME(6) NOT NULL,
    PRIMARY KEY (token_id),
    KEY idx_revoked_access_tokens_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 예약 요청 Idempotency-Key. 예약과 같은 트랜잭션에 저장하고, 하루가 지나면 애플리케이션이 지운다.
CREATE TABLE reservation_idempotency_keys (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    idempotency_key VARCHAR(100) NOT NULL,
    reservation_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_reservation_idempotency_user_key UNIQUE (user_id, idempotency_key),
    KEY idx_reservation_idempotency_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 도메인 이벤트 outbox. 비즈니스 데이터와 같은 트랜잭션에 쌓고, 따로 Kafka로 보낸 뒤 sent_at을 채운다.
CREATE TABLE outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    aggregate_type VARCHAR(50) NOT NULL COMMENT 'user, station, reservation',
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL COMMENT '이벤트 스냅샷 JSON',
    occurred_at DATETIME(6) NOT NULL,
    sent_at DATETIME(6) NULL COMMENT 'Kafka 전송 완료 시각',
    PRIMARY KEY (id),
    KEY idx_outbox_unsent (sent_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 애플리케이션에서 추가로 보장할 조건:
-- 1. 동일 충전구의 유효 예약 시간이 겹치지 않도록 동시 요청도 제어합니다.
--    예: 트랜잭션에서 해당 chargers 행을 SELECT ... FOR UPDATE로 잠근 뒤,
--    CONFIRMED 예약 중 starts_at < 새 ends_at AND ends_at > 새 starts_at을 조회합니다.
--    중복이 없을 때 INSERT 후 COMMIT합니다. 모든 예약 생성/시간 변경 경로에 적용합니다.
-- 2. 예약자의 차종(users.vehicle_model)을 충전소가 지원하는지(station_supported_models),
--    충전구가 운영 중인지 확인합니다.
-- 3. 설치현황은 개별 충전구 ID/규격/출력을 제공하지 않습니다.
--    가상 충전구를 생성할 경우 data_source = 'SIMULATION'으로 저장합니다.
