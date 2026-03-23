package com.e103.ohmyguide.domain.recommend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefreshRequest {
    private Double latitude;
    private Double longitude;
    private Double radiusKm;
    private String category;
    private String mood;
    private String freeText;
    private List<Integer> excludedAttrIds;
}
