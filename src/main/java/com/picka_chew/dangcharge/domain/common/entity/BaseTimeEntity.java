package com.picka_chew.dangcharge.domain.common.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * 생성 시각을 갖는 엔티티의 공통 부모. 스키마에 수정 시각 컬럼이 없어 created_at만 둔다.
 * 시각은 UTC 기준이다.
 */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity {

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
