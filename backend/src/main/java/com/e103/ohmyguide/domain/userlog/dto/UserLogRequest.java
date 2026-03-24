package com.e103.ohmyguide.domain.userlog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String nationality;

    @Positive
    private int age;

    @NotBlank
    private String gender;

    @NotBlank
    private String travelPurpose;

    @NotBlank
    private String lifestyle;

    @NotBlank
    private String action;

    @NotNull
    private Long placeId;

    @NotBlank
    private String timestamp;
}