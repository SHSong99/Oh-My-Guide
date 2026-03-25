package com.e103.ohmyguide.domain.attraction.controller.request;

import com.e103.ohmyguide.domain.attraction.service.request.AttractionCreateServiceRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class AttractionCreateRequest {

    @NotBlank
    private String title;

    @NotNull
    private BigDecimal latitude;

    @NotNull
    private BigDecimal longitude;

    private String firstImage1;

    private String overview;

    private String overviewTts;

    public AttractionCreateServiceRequest toServiceRequest() {
        return AttractionCreateServiceRequest.of(title, latitude, longitude, firstImage1, overview, overviewTts);
    }
}
