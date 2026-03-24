package com.e103.ohmyguide.domain.attraction.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GuideMessageResponse {

    private String guideMessage;

    public static GuideMessageResponse from(String guideMessage) {
        return new GuideMessageResponse(guideMessage);
    }
}
