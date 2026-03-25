package com.e103.ohmyguide.domain.attraction.service.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AttractionCreateServiceRequest {

    private final String title;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String firstImage1;
    private final String overview;
    private final String overviewTts;

    private AttractionCreateServiceRequest(String title, BigDecimal latitude, BigDecimal longitude,
                                           String firstImage1, String overview, String overviewTts) {
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.firstImage1 = firstImage1;
        this.overview = overview;
        this.overviewTts = overviewTts;
    }

    public static AttractionCreateServiceRequest of(String title, BigDecimal latitude, BigDecimal longitude,
                                                    String firstImage1, String overview, String overviewTts) {
        return new AttractionCreateServiceRequest(title, latitude, longitude, firstImage1, overview, overviewTts);
    }
}
