package com.picka_chew.dangcharge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Domain Service 레이어 컴포넌트를 표시하는 어노테이션. 도메인의 생성·변경 로직을 담당하는 클래스에 사용
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DomainService {

	@AliasFor(annotation = Component.class)
	String value() default "";

}
