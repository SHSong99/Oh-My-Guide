package com.e103.ohmyguide.domain.theme.controller.request;

import com.e103.ohmyguide.domain.theme.service.request.ThemeUpdateServiceRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThemeUpdateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String category;

    private String region;

    public ThemeUpdateServiceRequest toServiceRequest() {
        return ThemeUpdateServiceRequest.of(name, description, category, region);
    }
}
