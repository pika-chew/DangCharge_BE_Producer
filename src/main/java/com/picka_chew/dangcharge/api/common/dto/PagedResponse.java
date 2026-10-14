package com.picka_chew.dangcharge.api.common.dto;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {

	public static <E, T> PagedResponse<T> of(Page<E> page, Function<E, T> mapper) {
		return new PagedResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
				page.getTotalElements());
	}

}
