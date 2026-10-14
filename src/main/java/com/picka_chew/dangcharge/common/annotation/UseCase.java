package com.picka_chew.dangcharge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * UseCase 레이어 컴포넌트를 표시하는 어노테이션. 유스케이스 하나의 흐름을 조합하는 클래스에 사용
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface UseCase {

	@AliasFor(annotation = Component.class)
	String value() default "";

}
