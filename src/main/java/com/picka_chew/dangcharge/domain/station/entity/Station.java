package com.picka_chew.dangcharge.domain.station.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "stations",
		uniqueConstraints = @UniqueConstraint(name = "uq_stations_external", columnNames = "external_id"))
public class Station {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 외부 공급원의 충전소 ID. 단일 외부 공급원 기준이며 없으면 null */
	@Column(name = "external_id", length = 100)
	private String externalId;

	/** 충전소 이름 */
	@Column(nullable = false, length = 200)
	private String name;

	@Column(nullable = false, length = 500)
	private String address;

	/** 지역 */
	@Column(nullable = false, length = 100)
	private String region;

	/** 위도. 없으면 null, 있으면 -90~90 */
	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	/** 경도. 없으면 null, 있으면 -180~180 */
	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	/** 마지막 동기화 시각(UTC) */
	@Column(name = "synced_at")
	private Instant syncedAt;

}
