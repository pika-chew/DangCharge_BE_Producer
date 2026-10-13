package com.picka_chew.dangcharge.domain.user.entity;

import com.picka_chew.dangcharge.domain.common.entity.BaseTimeEntity;

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

	/** 차량번호 */
	@Column(name = "vehicle_number", nullable = false, length = 20)
	private String vehicleNumber;

}
