package com.e103.ohmyguide.domain.theme.controller.request;

import com.e103.ohmyguide.domain.theme.service.request.ThemeAttractionAddServiceRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ThemeAttractionAddRequest {

    @NotNull
    private Long attractionId;

    @NotNull
    @Positive
    private Integer attractionOrder;

    public ThemeAttractionAddServiceRequest toServiceRequest() {
        return ThemeAttractionAddServiceRequest.of(attractionId, attractionOrder);
    }
}
