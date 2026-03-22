package com.e103.ohmyguide.domain.attraction.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttractionServiceTest extends IntegrationTestSupport {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private AttractionRepository attractionRepository;

    @DisplayName("Attraction ID로 가이드 메시지를 조회한다.")
    @Test
    void getGuideMessageBy_returnsOverviewTts() {
        // given
        Attraction attraction = buildAttraction("서울타워", "서울타워는 남산에 위치한...");
        attractionRepository.save(attraction);

        // when
        String guideMessage = attractionService.getGuideMessageBy(attraction.getId());

        // then
        assertThat(guideMessage).isEqualTo("서울타워는 남산에 위치한...");
    }

    @DisplayName("존재하지 않는 Attraction ID로 조회 시 예외가 발생한다.")
    @Test
    void getGuideMessageBy_attractionNotFound_throwsException() {
        // given
        Long invalidAttractionId = 999L;

        // when & then
        assertThatThrownBy(() -> attractionService.getGuideMessageBy(invalidAttractionId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Attraction not found");
    }

    @DisplayName("overviewTts가 null인 경우에도 null을 반환한다.")
    @Test
    void getGuideMessageBy_overviewTtsIsNull_returnsNull() {
        // given
        Attraction attraction = buildAttraction("서울타워", null);
        attractionRepository.save(attraction);

        // when
        String guideMessage = attractionService.getGuideMessageBy(attraction.getId());

        // then
        assertThat(guideMessage).isNull();
    }

    @DisplayName("overviewTts가 빈 문자열인 경우 빈 문자열을 반환한다.")
    @Test
    void getGuideMessageBy_overviewTtsIsEmpty_returnsEmpty() {
        // given
        Attraction attraction = buildAttraction("서울타워", "");
        attractionRepository.save(attraction);

        // when
        String guideMessage = attractionService.getGuideMessageBy(attraction.getId());

        // then
        assertThat(guideMessage).isEmpty();
    }

    private Attraction buildAttraction(String title, String overviewTts) {
        Attraction attraction = Attraction.builder()
                .contentId(12345)
                .title(title)
                .addr1("서울특별시 용산구")
                .latitude(BigDecimal.valueOf(37.5511))
                .longitude(BigDecimal.valueOf(126.9882))
                .overview("Overview text")
                .build();

        // overviewTts는 Builder에 없으므로 Reflection으로 설정
        ReflectionTestUtils.setField(attraction, "overviewTts", overviewTts);

        return attraction;
    }
}
