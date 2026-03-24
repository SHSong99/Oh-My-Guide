package com.e103.ohmyguide.domain.guide.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StartLocationResponse {

    private BigDecimal latitude;
    private BigDecimal longitude;
}
