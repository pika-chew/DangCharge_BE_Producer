package com.picka_chew.dangcharge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * Validator 레이어 컴포넌트를 표시하는 어노테이션. 검증만 하고 통과하지 못하면 예외를 던지는 클래스에 사용
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Validator {

	@AliasFor(annotation = Component.class)
	String value() default "";

}
