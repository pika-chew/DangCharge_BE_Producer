package com.picka_chew.dangcharge.domain.station.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 충전소. 한전 설치현황 한 건에 대응한다. 설치현황에는 충전소 ID가 없어 이름과 주소로 같은 충전소인지 구분한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(of = "id")
@Table(name = "stations",
		uniqueConstraints = {
				@UniqueConstraint(name = "uq_stations_external", columnNames = "external_id"),
				@UniqueConstraint(name = "uq_stations_name_address", columnNames = {"name", "address"})
		},
		indexes = @Index(name = "idx_stations_metro_city", columnList = "metro, city"))
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

	/** 시도 */
	@Column(nullable = false, length = 50)
	private String metro;

	/** 시군구 */
	@Column(nullable = false, length = 50)
	private String city;

	/** 급속 충전기 수 */
	@Column(name = "rapid_count", nullable = false)
	private int rapidCount;

	/** 완속 충전기 수 */
	@Column(name = "slow_count", nullable = false)
	private int slowCount;

	/** 이 충전소에서 충전할 수 있는 차종 */
	@Builder.Default
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "station_supported_models",
			joinColumns = @JoinColumn(name = "station_id"),
			foreignKey = @ForeignKey(name = "fk_station_models_station"),
			indexes = @Index(name = "idx_station_models_model", columnList = "vehicle_model"))
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "vehicle_model", nullable = false, length = 30)
	private Set<VehicleModel> supportedModels = new HashSet<>();

	/** 위도. 없으면 null, 있으면 -90~90 */
	@Column(precision = 10, scale = 7)
	private BigDecimal latitude;

	/** 경도. 없으면 null, 있으면 -180~180 */
	@Column(precision = 10, scale = 7)
	private BigDecimal longitude;

	/** 마지막 동기화 시각(UTC) */
	@Column(name = "synced_at")
	private Instant syncedAt;

	/** 주어진 차종을 이 충전소에서 충전할 수 있는지 */
	public boolean supports(VehicleModel model) {
		return supportedModels.contains(model);
	}

	/**
	 * 외부 설치현황의 최신 값으로 덮어쓴다. 이름과 주소는 충전소를 구분하는 값이라 바꾸지 않는다.
	 *
	 * @return 값이 하나라도 바뀌었으면 true. 동기화 시각만 바뀐 경우는 false다
	 */
	public boolean sync(String metro, String city, int rapidCount, int slowCount,
			Collection<VehicleModel> supportedModels, Instant syncedAt) {
		boolean changed = !this.metro.equals(metro) || !this.city.equals(city) || this.rapidCount != rapidCount
				|| this.slowCount != slowCount || !this.supportedModels.equals(new HashSet<>(supportedModels));
		this.metro = metro;
		this.city = city;
		this.rapidCount = rapidCount;
		this.slowCount = slowCount;
		this.supportedModels.retainAll(supportedModels);
		this.supportedModels.addAll(supportedModels);
		this.syncedAt = syncedAt;
		return changed;
	}

}
