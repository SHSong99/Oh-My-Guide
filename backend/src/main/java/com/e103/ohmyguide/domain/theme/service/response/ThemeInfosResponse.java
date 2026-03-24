package com.e103.ohmyguide.domain.theme.service.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ThemeInfosResponse {

    private int count;
    private List<ThemeInfoResponse> themes;

    public static ThemeInfosResponse of(List<ThemeInfoResponse> themes) {
        return ThemeInfosResponse.builder()
                .count(themes.size())
                .themes(themes)
                .build();
    }
}
