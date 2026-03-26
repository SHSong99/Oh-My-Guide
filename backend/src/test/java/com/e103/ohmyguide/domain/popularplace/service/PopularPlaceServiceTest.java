package com.e103.ohmyguide.domain.popularplace.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.popularplace.dto.PopularPlaceResponse;
import com.e103.ohmyguide.domain.popularplace.entity.PopularPlace;
import com.e103.ohmyguide.domain.popularplace.repository.PopularPlaceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PopularPlaceServiceTest extends IntegrationTestSupport {

    @Autowired
    private PopularPlaceService popularPlaceService;

    @Autowired
    private PopularPlaceRepository popularPlaceRepository;

    @DisplayName("군집 조건으로 인기 장소 추천 목록을 DTO로 반환한다.")
    @Test
    void getRecommendations_returnsMappedDtoList() {
        // given
        popularPlaceRepository.save(buildPopularPlace(100L, 50L, 200L, 1));
        popularPlaceRepository.save(buildPopularPlace(200L, 30L, 120L, 2));

        // when
        List<PopularPlaceResponse> result = popularPlaceService.getRecommendations(
                "KOR", "20s", "M", "LEISURE"
        );

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlaceId()).isEqualTo(100L);
        assertThat(result.get(0).getVisitCount()).isEqualTo(50L);
        assertThat(result.get(0).getTotalScore()).isEqualTo(200L);
        assertThat(result.get(0).getPlaceRank()).isEqualTo(1);
        assertThat(result.get(1).getPlaceId()).isEqualTo(200L);
        assertThat(result.get(1).getPlaceRank()).isEqualTo(2);
    }

    @DisplayName("조건에 맞는 인기 장소가 없으면 빈 리스트를 반환한다.")
    @Test
    void getRecommendations_returnsEmptyList() {
        // given (저장 없음)

        // when
        List<PopularPlaceResponse> result = popularPlaceService.getRecommendations(
                "KOR", "20s", "M", "LEISURE"
        );

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("다른 군집 조건의 데이터는 결과에 포함되지 않는다.")
    @Test
    void getRecommendations_excludesOtherClusters() {
        // given
        popularPlaceRepository.save(buildPopularPlace(100L, 50L, 200L, 1));
        popularPlaceRepository.save(PopularPlace.builder()
                .nationality("USA")
                .ageGroup("30s")
                .gender("F")
                .travelPurpose("BUSINESS")
                .placeId(999L)
                .visitCount(10L)
                .totalScore(40L)
                .placeRank(1)
                .build());

        // when
        List<PopularPlaceResponse> result = popularPlaceService.getRecommendations(
                "KOR", "20s", "M", "LEISURE"
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlaceId()).isEqualTo(100L);
    }

    @DisplayName("Entity의 필드가 DTO에 올바르게 매핑된다.")
    @Test
    void getRecommendations_entityMappedCorrectlyToDto() {
        // given
        popularPlaceRepository.save(buildPopularPlace(999L, 77L, 333L, 1));

        // when
        List<PopularPlaceResponse> result = popularPlaceService.getRecommendations(
                "KOR", "20s", "M", "LEISURE"
        );

        // then
        assertThat(result).hasSize(1);
        PopularPlaceResponse response = result.get(0);
        assertThat(response.getPlaceId()).isEqualTo(999L);
        assertThat(response.getVisitCount()).isEqualTo(77L);
        assertThat(response.getTotalScore()).isEqualTo(333L);
        assertThat(response.getPlaceRank()).isEqualTo(1);
    }

    @DisplayName("7개 저장 시 placeRank 기준 상위 5개만 반환된다.")
    @Test
    void getRecommendations_limitedToFive() {
        // given
        for (int rank = 1; rank <= 7; rank++) {
            popularPlaceRepository.save(buildPopularPlace((long) rank * 100, 50L, 200L, rank));
        }

        // when
        List<PopularPlaceResponse> result = popularPlaceService.getRecommendations(
                "KOR", "20s", "M", "LEISURE"
        );

//        // then
//        assertThat(result).hasSize(5);
//        assertThat(result.get(0).getPlaceRank()).isEqualTo(1);
//        assertThat(result.get(4).getPlaceRank()).isEqualTo(5);

        assertThat(result).hasSize(5)
                .extracting(PopularPlaceResponse::getPlaceRank)
                .containsExactly(1, 2, 3, 4, 5);
    }

    private PopularPlace buildPopularPlace(Long placeId, Long visitCount, Long totalScore, Integer placeRank) {
        return PopularPlace.builder()
                .nationality("KOR")
                .ageGroup("20s")
                .gender("M")
                .travelPurpose("LEISURE")
                .placeId(placeId)
                .visitCount(visitCount)
                .totalScore(totalScore)
                .placeRank(placeRank)
                .build();
    }
}
