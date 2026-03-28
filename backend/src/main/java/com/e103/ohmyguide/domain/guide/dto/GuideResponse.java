package com.e103.ohmyguide.domain.guide.dto;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideResponse {

    private Long placeId;
    private String title;
    private String addr1;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String firstImage1;
    private String overview;
    private String overviewTts;

    public static GuideResponse from(Attraction attraction) {
        return GuideResponse.builder()
                .placeId(attraction.getId())
                .title(attraction.getTitle())
                .addr1(attraction.getAddr1())
                .latitude(attraction.getLatitude())
                .longitude(attraction.getLongitude())
                .firstImage1(attraction.getFirstImage1())
                .overview(attraction.getOverview())
                .overviewTts(attraction.getOverviewTts())
                .build();
    }
}
