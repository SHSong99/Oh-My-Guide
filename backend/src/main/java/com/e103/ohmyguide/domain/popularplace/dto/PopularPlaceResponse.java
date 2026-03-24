package com.e103.ohmyguide.domain.popularplace.dto;

import com.e103.ohmyguide.domain.popularplace.entity.PopularPlace;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder                    // .builder().placeId(1001).totalScore(8).build() 패턴 사용 가능
public class PopularPlaceResponse {

    private Long placeId;        // 어떤 장소?
    private Long visitCount;     // 몇 번 방문?
    private Long totalScore;     // 총 점수?
    private Integer placeRank;   // 몇 위?

    // Entity → Response DTO 변환 메서드
    // Entity의 10개 필드 중 4개만 골라서 반환
    public static PopularPlaceResponse from(PopularPlace entity) {
        return PopularPlaceResponse.builder()
                .placeId(entity.getPlaceId())
                .visitCount(entity.getVisitCount())
                .totalScore(entity.getTotalScore())
                .placeRank(entity.getPlaceRank())
                .build();
    }
}