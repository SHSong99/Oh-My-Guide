package com.e103.ohmyguide.domain.theme.service.request;

import lombok.Getter;

@Getter
public class ThemeCreateServiceRequest {

    private final String name;
    private final String description;
    private final String category;
    private final String region;

    private ThemeCreateServiceRequest(String name, String description, String category, String region) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
    }

    public static ThemeCreateServiceRequest of(String name, String description, String category, String region) {
        return new ThemeCreateServiceRequest(name, description, category, region);
    }
}
