package com.picka_chew.dangcharge.domain.vehicle.entity;

import java.math.BigDecimal;

import com.picka_chew.dangcharge.domain.common.entity.BaseTimeEntity;
import com.picka_chew.dangcharge.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "vehicles", indexes = @Index(name = "idx_vehicles_user", columnList = "user_id"))
public class Vehicle extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_vehicles_user"))
	private User user;

	/** 차종 */
	@Column(nullable = false, length = 100)
	private String model;

	/** 충전 규격 */
	@Column(name = "connector_type", nullable = false, length = 50)
	private String connectorType;

	/** 배터리 용량(kWh). 없으면 null, 있으면 0보다 크다 */
	@Column(name = "battery_capacity_kwh", precision = 8, scale = 2)
	private BigDecimal batteryCapacityKwh;

	/** 현재 잔량(%). 없으면 null, 있으면 0~100 */
	@Column(name = "current_soc_percent", precision = 5, scale = 2)
	private BigDecimal currentSocPercent;

}
