package com.e103.ohmyguide.domain.recommend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class RefreshResponse {
    private List<PlaceCardResponse> recommendations;
}
