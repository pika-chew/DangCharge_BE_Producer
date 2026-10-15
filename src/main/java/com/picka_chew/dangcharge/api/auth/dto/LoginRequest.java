package com.picka_chew.dangcharge.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String loginId, @NotBlank String password) {
}
