package com.e103.ohmyguide.domain.theme.service.request;

import lombok.Getter;

@Getter
public class ThemeUpdateServiceRequest {

    private final String name;
    private final String description;
    private final String category;
    private final String region;

    private ThemeUpdateServiceRequest(String name, String description, String category, String region) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
    }

    public static ThemeUpdateServiceRequest of(String name, String description, String category, String region) {
        return new ThemeUpdateServiceRequest(name, description, category, region);
    }
}
