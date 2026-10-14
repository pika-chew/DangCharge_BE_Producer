package com.picka_chew.dangcharge.api.common.dto;

import java.util.List;

public record PagedResponse<T>(List<T> content, int page, int size, long totalElements) {
}
