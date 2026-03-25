package com.e103.ohmyguide.domain.theme.controller.request;

import com.e103.ohmyguide.domain.theme.service.request.ThemeCreateServiceRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThemeCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String category;

    private String region;

    public ThemeCreateServiceRequest toServiceRequest() {
        return ThemeCreateServiceRequest.of(name, description, category, region);
    }
}
