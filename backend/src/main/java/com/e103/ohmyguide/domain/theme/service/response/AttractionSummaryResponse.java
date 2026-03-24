package com.e103.ohmyguide.domain.theme.service.response;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AttractionSummaryResponse {

    private Long attractionId;
    private String image;
    private String title;
    private String overview;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public static AttractionSummaryResponse from(Attraction attraction) {
        return AttractionSummaryResponse.builder()
                .attractionId(attraction.getId())
                .image(attraction.getFirstImage1())
                .title(attraction.getTitle())
                .overview(attraction.getOverview())
                .latitude(attraction.getLatitude())
                .longitude(attraction.getLongitude())
                .build();
    }
}
