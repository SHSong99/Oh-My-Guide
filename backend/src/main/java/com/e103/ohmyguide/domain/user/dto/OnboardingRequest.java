package com.e103.ohmyguide.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OnboardingRequest {

    @NotBlank
    private String nationality;

    @NotNull
    @Positive
    private Integer age;

    @NotBlank
    private String gender;

    private String companion;

    private String country;
}
