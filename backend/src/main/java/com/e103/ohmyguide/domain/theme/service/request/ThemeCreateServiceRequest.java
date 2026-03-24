package com.e103.ohmyguide.domain.theme.service.request;

import lombok.Getter;

@Getter
public class ThemeCreateServiceRequest {

    private final String name;
    private final String description;

    private ThemeCreateServiceRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public static ThemeCreateServiceRequest of(String name, String description) {
        return new ThemeCreateServiceRequest(name, description);
    }
}
