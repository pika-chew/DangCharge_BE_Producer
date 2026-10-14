package com.picka_chew.dangcharge.domain.user.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.picka_chew.dangcharge.domain.common.entity.BaseTimeEntity;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uq_users_login_id", columnNames = "login_id"))
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 로그인 아이디 */
	@Column(name = "login_id", nullable = false, length = 50)
	private String loginId;

	/** 비밀번호 해시 */
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	/** 유저네임 */
	@Column(nullable = false, length = 50)
	private String username;

	/** 차종. 정해진 선택지 중 하나이며 충전소의 지원 차종과 비교하는 기준이다 */
	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "vehicle_model", nullable = false, length = 30)
	private VehicleModel vehicleModel;

	/** 프로필을 바꾼다. null인 값은 그대로 둔다 */
	public void updateProfile(String username, VehicleModel vehicleModel) {
		if (username != null) {
			this.username = username;
		}
		if (vehicleModel != null) {
			this.vehicleModel = vehicleModel;
		}
	}

}
