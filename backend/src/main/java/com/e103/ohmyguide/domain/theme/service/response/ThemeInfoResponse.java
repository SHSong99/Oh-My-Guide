package com.e103.ohmyguide.domain.theme.service.response;

import com.e103.ohmyguide.domain.theme.entity.Theme;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThemeInfoResponse {

    private Long themeId;
    private String name;
    private String description;
    private String category;
    private String region;

    public static ThemeInfoResponse from(Theme theme) {
        return ThemeInfoResponse.builder()
                .themeId(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .category(theme.getCategory())
                .region(theme.getRegion())
                .build();
    }
}
