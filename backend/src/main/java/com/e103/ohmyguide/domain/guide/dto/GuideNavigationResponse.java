package com.e103.ohmyguide.domain.guide.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GuideNavigationResponse {

    private StartLocationResponse startLocation;
    private GuideResponse destination;
    private List<GuideResponse> nearbyPlaces;
}
