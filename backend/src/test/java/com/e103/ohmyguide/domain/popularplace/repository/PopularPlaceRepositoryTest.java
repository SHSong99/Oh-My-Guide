package com.e103.ohmyguide.domain.popularplace.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.popularplace.entity.PopularPlace;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PopularPlaceRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private PopularPlaceRepository popularPlaceRepository;

    @DisplayName("인기 장소를 저장하고 ID로 조회할 수 있다.")
    @Test
    void saveAndFindById() {
        // given
        PopularPlace popularPlace = PopularPlace.builder()
                .nationality("KOR")
                .ageGroup("20s")
                .gender("M")
                .travelPurpose("LEISURE")
                .lifestyle("ACTIVE")
                .placeId(100L)
                .visitCount(50L)
                .totalScore(200L)
                .placeRank(1)
                .build();

        // when
        PopularPlace saved = popularPlaceRepository.save(popularPlace);
        Optional<PopularPlace> found = popularPlaceRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getNationality()).isEqualTo("KOR");
        assertThat(found.get().getAgeGroup()).isEqualTo("20s");
        assertThat(found.get().getGender()).isEqualTo("M");
        assertThat(found.get().getTravelPurpose()).isEqualTo("LEISURE");
        assertThat(found.get().getLifestyle()).isEqualTo("ACTIVE");
        assertThat(found.get().getPlaceId()).isEqualTo(100L);
        assertThat(found.get().getPlaceRank()).isEqualTo(1);
    }

    @DisplayName("군집 조건으로 인기 장소를 조회하면 placeRank 오름차순으로 최대 5개 반환된다.")
    @Test
    void findByCluster_returnsSortedByRankAndLimitedToFive() {
        // given - 7개 저장
        for (int rank = 1; rank <= 7; rank++) {
            popularPlaceRepository.save(PopularPlace.builder()
                    .nationality("KOR")
                    .ageGroup("20s")
                    .gender("M")
                    .travelPurpose("LEISURE")
                    .lifestyle("ACTIVE")
                    .placeId((long) rank * 100)
                    .visitCount(50L)
                    .totalScore(200L)
                    .placeRank(rank)
                    .build());
        }

        // when
        List<PopularPlace> result = popularPlaceRepository.findByCluster(
                "KOR", "20s", "M", "LEISURE", "ACTIVE"
        );

        // then
        assertThat(result).hasSize(5);
        assertThat(result.get(0).getPlaceRank()).isEqualTo(1);
        assertThat(result.get(4).getPlaceRank()).isEqualTo(5);
    }

    @DisplayName("군집 조건이 다른 데이터는 조회되지 않는다.")
    @Test
    void findByCluster_doesNotReturnOtherClusters() {
        // given
        PopularPlace target = PopularPlace.builder()
                .nationality("KOR")
                .ageGroup("20s")
                .gender("M")
                .travelPurpose("LEISURE")
                .lifestyle("ACTIVE")
                .placeId(100L)
                .visitCount(50L)
                .totalScore(200L)
                .placeRank(1)
                .build();

        PopularPlace other = PopularPlace.builder()
                .nationality("USA")
                .ageGroup("30s")
                .gender("F")
                .travelPurpose("BUSINESS")
                .lifestyle("RELAX")
                .placeId(999L)
                .visitCount(10L)
                .totalScore(40L)
                .placeRank(1)
                .build();

        popularPlaceRepository.save(target);
        popularPlaceRepository.save(other);

        // when
        List<PopularPlace> result = popularPlaceRepository.findByCluster(
                "KOR", "20s", "M", "LEISURE", "ACTIVE"
        );

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNationality()).isEqualTo("KOR");
        assertThat(result.get(0).getPlaceId()).isEqualTo(100L);
    }

    @DisplayName("조건에 맞는 데이터가 없으면 빈 리스트를 반환한다.")
    @Test
    void findByCluster_returnsEmptyWhenNoMatch() {
        // given (저장 없음)

        // when
        List<PopularPlace> result = popularPlaceRepository.findByCluster(
                "KOR", "20s", "M", "LEISURE", "ACTIVE"
        );

        // then
        assertThat(result).isEmpty();
    }
}
