package com.e103.ohmyguide.domain.theme.response;

import com.e103.ohmyguide.domain.theme.entity.Theme;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ThemeDetailResponse {

    private Long themeId;
    private String name;
    private String description;
    private int attractionCount;
    private List<AttractionSummaryResponse> attractions;

    public static ThemeDetailResponse of(Theme theme, List<AttractionSummaryResponse> attractions) {
        return ThemeDetailResponse.builder()
                .themeId(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .attractionCount(attractions.size())
                .attractions(attractions)
                .build();
    }
}
