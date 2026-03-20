package com.e103.ohmyguide.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(@NotBlank String accessToken) {
}
