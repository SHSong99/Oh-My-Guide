package com.e103.ohmyguide.domain.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AiRefreshRequest {
    @JsonProperty("user_id")
    private Long userId;
    private Double latitude;
    private Double longitude;
    @JsonProperty("radius_km")
    private Double radiusKm;
    private String category;
    private String mood;
    @JsonProperty("free_text")
    private String freeText;
    @JsonProperty("excluded_attr_ids")
    private List<Integer> excludedAttrIds;
}
