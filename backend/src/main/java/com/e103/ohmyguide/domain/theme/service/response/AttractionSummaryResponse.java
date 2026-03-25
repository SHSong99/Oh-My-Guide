package com.e103.ohmyguide.domain.theme.service.response;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
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
    private String overviewTts;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer attractionOrder;

    public static AttractionSummaryResponse from(ThemeAttraction themeAttraction) {
        Attraction attraction = themeAttraction.getAttraction();
        return AttractionSummaryResponse.builder()
                .attractionId(attraction.getId())
                .image(attraction.getFirstImage1())
                .title(attraction.getTitle())
                .overview(attraction.getOverview())
                .overviewTts(attraction.getOverviewTts())
                .latitude(attraction.getLatitude())
                .longitude(attraction.getLongitude())
                .attractionOrder(themeAttraction.getAttractionOrder())
                .build();
    }
}
