package com.e103.ohmyguide.domain.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideNavigationResponse {

    private StartLocationResponse startLocation;
    private GuideResponse destination;
    private List<GuideResponse> nearbyPlaces;
}
