package com.e103.ohmyguide.domain.attraction.controller.request;

import com.e103.ohmyguide.domain.attraction.service.request.AttractionUpdateServiceRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AttractionUpdateRequest {

    private String title;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String firstImage1;
    private String overview;
    private String overviewTts;

    public AttractionUpdateServiceRequest toServiceRequest() {
        return AttractionUpdateServiceRequest.of(title, latitude, longitude, firstImage1, overview, overviewTts);
    }
}
