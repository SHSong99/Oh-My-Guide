package com.e103.ohmyguide.domain.theme.service.request;

import lombok.Getter;

@Getter
public class ThemeUpdateServiceRequest {

    private final String name;
    private final String description;

    private ThemeUpdateServiceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static ThemeUpdateServiceRequest of(String name, String description) {
        return new ThemeUpdateServiceRequest(name, description);
    }
}
