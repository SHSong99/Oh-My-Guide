package com.e103.ohmyguide.domain.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttractionsRouteResponseMessage {

    private Long userId;
    private GuideNavigationResponse navigationResponse;
}
