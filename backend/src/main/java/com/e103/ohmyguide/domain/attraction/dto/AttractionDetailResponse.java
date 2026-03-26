package com.e103.ohmyguide.domain.attraction.dto;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttractionDetailResponse {
    private Long attrId;
    private Integer contentId;
    private String title;
    private String addr1;
    private String tel;
    private Double latitude;
    private Double longitude;
    private String firstImage1;
    private String firstImage2;
    private String overview;
    private String overviewTts;
    private String homepage;
    private Long contentTypeId;

    public static AttractionDetailResponse from(Attraction a) {
        return AttractionDetailResponse.builder()
                .attrId(a.getId())
                .contentId(a.getContentId())
                .title(a.getTitle())
                .addr1(a.getAddr1())
                .tel(a.getTel())
                .latitude(a.getLatitude() != null ? a.getLatitude().doubleValue() : null)
                .longitude(a.getLongitude() != null ? a.getLongitude().doubleValue() : null)
                .firstImage1(a.getFirstImage1())
                .firstImage2(a.getFirstImage2())
                .overview(a.getOverview())
                .overviewTts(a.getOverviewTts())
                .homepage(a.getHomepage())
                .contentTypeId(a.getContentType() != null ? a.getContentType().getContentTypeId() : null)
                .build();
    }
}
