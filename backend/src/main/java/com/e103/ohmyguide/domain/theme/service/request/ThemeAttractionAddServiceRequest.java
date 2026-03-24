package com.e103.ohmyguide.domain.theme.service.request;

import lombok.Getter;

@Getter
public class ThemeAttractionAddServiceRequest {

    private final Long attractionId;
    private final Integer attractionOrder;

    private ThemeAttractionAddServiceRequest(Long attractionId, Integer attractionOrder) {
        this.attractionId = attractionId;
        this.attractionOrder = attractionOrder;
    }

    public static ThemeAttractionAddServiceRequest of(Long attractionId, Integer attractionOrder) {
        return new ThemeAttractionAddServiceRequest(attractionId, attractionOrder);
    }
}
