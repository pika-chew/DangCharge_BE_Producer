package com.picka_chew.dangcharge.domain.station.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "chargers",
		uniqueConstraints = @UniqueConstraint(name = "uq_chargers_external",
				columnNames = {"station_id", "external_id"}))
public class Charger {

	private static final BigDecimal SIMULATED_FAST_KW = new BigDecimal("50.00");
	private static final BigDecimal SIMULATED_SLOW_KW = new BigDecimal("7.00");

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "station_id", nullable = false, foreignKey = @ForeignKey(name = "fk_chargers_station"))
	private Station station;

	/** 충전소 안에서의 외부 ID. 없으면 null */
	@Column(name = "external_id", length = 100)
	private String externalId;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "charging_type", nullable = false, length = 20)
	private ChargingType chargingType;

	/** 충전 규격. 없으면 null */
	@Column(name = "connector_type", length = 50)
	private String connectorType;

	/** 최대 출력(kW). 없으면 null, 있으면 0보다 크다 */
	@Column(name = "max_power_kw", precision = 8, scale = 2)
	private BigDecimal maxPowerKw;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "operational_status", nullable = false, length = 30)
	private OperationalStatus operationalStatus = OperationalStatus.UNKNOWN;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "data_source", nullable = false, length = 20)
	private ChargerDataSource dataSource;

	/**
	 * 설치현황의 충전기 수만큼 만드는 가상 충전기. 개별 출력 정보가 없어 급속 50kW, 완속 7kW로 둔다.
	 */
	public static Charger simulated(Station station, ChargingType chargingType) {
		return Charger.builder()
				.station(station)
				.chargingType(chargingType)
				.maxPowerKw(chargingType == ChargingType.FAST ? SIMULATED_FAST_KW : SIMULATED_SLOW_KW)
				.operationalStatus(OperationalStatus.AVAILABLE)
				.dataSource(ChargerDataSource.SIMULATION)
				.build();
	}

	public void changeOperationalStatus(OperationalStatus operationalStatus) {
		this.operationalStatus = operationalStatus;
	}

	/** 고장·점검 중이 아니면 예약할 수 있다. 상태를 모르는 충전기(UNKNOWN)도 예약은 받는다 */
	public boolean isUsable() {
		return operationalStatus != OperationalStatus.OUT_OF_SERVICE;
	}

}
