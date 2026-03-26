package com.e103.ohmyguide.domain.guide.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class GuideGoResponse {
    private StartLocationResponse startLocation;
    private GuideResponse destination;

    @Builder
    public GuideGoResponse(StartLocationResponse startLocation, GuideResponse destination) {
        this.startLocation = startLocation;
        this.destination = destination;
    }
}

