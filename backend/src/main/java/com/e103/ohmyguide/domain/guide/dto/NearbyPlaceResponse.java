package com.e103.ohmyguide.domain.guide.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 경로 주변 장소 목록용 경량 DTO.
 * 메시지 크기를 줄이기 위해 placeId·좌표만 담는다.
 * 상세 정보(title/overview/image 등)는 프런트에서 placeId로 별도 조회.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyPlaceResponse {

    private Long placeId;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
