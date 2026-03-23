package com.e103.ohmyguide.domain.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlaceCardResponse {
    @JsonProperty("attr_id")
    private Integer attrId;
    private String name;
    @JsonProperty("name_kr")
    private String nameKr;
    @JsonProperty("image_url")
    private String imageUrl;
    private String distance;
    private String tag;
    private Double latitude;
    private Double longitude;
}
