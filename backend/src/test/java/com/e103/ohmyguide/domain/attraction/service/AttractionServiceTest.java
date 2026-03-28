package com.e103.ohmyguide.domain.attraction.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionCreateServiceRequest;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionUpdateServiceRequest;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    @DisplayName("관광지를 등록하면 DB에 저장되고 응답 DTO를 반환한다.")
    @Test
    void createAttraction_savedSuccessfully() {
        // given
        AttractionCreateServiceRequest request = AttractionCreateServiceRequest.of(
                "한라산",
                new BigDecimal("33.36160800"),
                new BigDecimal("126.53390800"),
                "http://image.url/hallasan.jpg",
                "한라산 개요",
                "한라산 TTS 개요"
        );

        // when
        AttractionDetailResponse response = attractionService.createAttraction(request);

        // then
        assertThat(attractionRepository.findAll()).hasSize(1);
        Attraction saved = attractionRepository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("한라산");
        assertThat(saved.getLatitude()).isEqualByComparingTo(new BigDecimal("33.36160800"));
        assertThat(saved.getLongitude()).isEqualByComparingTo(new BigDecimal("126.53390800"));
        assertThat(saved.getFirstImage1()).isEqualTo("http://image.url/hallasan.jpg");
        assertThat(saved.getOverview()).isEqualTo("한라산 개요");
        assertThat(saved.getOverviewTts()).isEqualTo("한라산 TTS 개요");
        assertThat(response.getAttrId()).isEqualTo(saved.getId());
        assertThat(response.getTitle()).isEqualTo("한라산");
    }

    @DisplayName("firstImage1, overview 없이 필수 필드만으로 관광지를 등록할 수 있다.")
    @Test
    void createAttraction_withoutOptionalFields() {
        // given
        AttractionCreateServiceRequest request = AttractionCreateServiceRequest.of(
                "성산일출봉",
                new BigDecimal("33.45840800"),
                new BigDecimal("126.94240800"),
                null,
                null,
                null
        );

        // when
        attractionService.createAttraction(request);

        // then
        Attraction saved = attractionRepository.findAll().get(0);
        assertThat(saved.getTitle()).isEqualTo("성산일출봉");
        assertThat(saved.getFirstImage1()).isNull();
        assertThat(saved.getOverview()).isNull();
        assertThat(saved.getOverviewTts()).isNull();
    }

    @DisplayName("관광지 title을 수정하면 DB에 반영된다.")
    @Test
    void updateAttraction_titleUpdated() {
        // given
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .latitude(new BigDecimal("33.36160800"))
                .longitude(new BigDecimal("126.53390800"))
                .build());
        AttractionUpdateServiceRequest request = AttractionUpdateServiceRequest.of(
                "한라산 국립공원", null, null, null, null, null);

        // when
        attractionService.updateAttraction(attraction.getId(), request);

        // then
        Attraction updated = attractionRepository.findById(attraction.getId()).get();
        assertThat(updated.getTitle()).isEqualTo("한라산 국립공원");
        assertThat(updated.getLatitude()).isEqualByComparingTo(new BigDecimal("33.36160800"));
        assertThat(updated.getLongitude()).isEqualByComparingTo(new BigDecimal("126.53390800"));
    }

    @DisplayName("관광지 위경도와 이미지를 수정하면 DB에 반영되고 나머지 필드는 유지된다.")
    @Test
    void updateAttraction_partialUpdate() {
        // given
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .latitude(new BigDecimal("33.36160800"))
                .longitude(new BigDecimal("126.53390800"))
                .firstImage1("old_image.jpg")
                .overview("기존 개요")
                .build());
        AttractionUpdateServiceRequest request = AttractionUpdateServiceRequest.of(
                null,
                new BigDecimal("33.50000000"),
                new BigDecimal("126.60000000"),
                "new_image.jpg",
                null,
                null
        );

        // when
        attractionService.updateAttraction(attraction.getId(), request);

        // then
        Attraction updated = attractionRepository.findById(attraction.getId()).get();
        assertThat(updated.getTitle()).isEqualTo("한라산");
        assertThat(updated.getLatitude()).isEqualByComparingTo(new BigDecimal("33.50000000"));
        assertThat(updated.getLongitude()).isEqualByComparingTo(new BigDecimal("126.60000000"));
        assertThat(updated.getFirstImage1()).isEqualTo("new_image.jpg");
        assertThat(updated.getOverview()).isEqualTo("기존 개요");
    }

    @DisplayName("updateAttraction은 수정된 관광지의 DTO를 반환한다.")
    @Test
    void updateAttraction_returnsUpdatedDto() {
        // given
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .latitude(new BigDecimal("33.36160800"))
                .longitude(new BigDecimal("126.53390800"))
                .build());
        AttractionUpdateServiceRequest request = AttractionUpdateServiceRequest.of(
                "한라산 국립공원", null, null, null, "새로운 개요", null);

        // when
        AttractionDetailResponse response = attractionService.updateAttraction(attraction.getId(), request);

        // then
        assertThat(response.getTitle()).isEqualTo("한라산 국립공원");
        assertThat(response.getOverview()).isEqualTo("새로운 개요");
    }

    @DisplayName("관광지 overviewTts를 수정하면 DB에 반영되고 나머지 필드는 유지된다.")
    @Test
    void updateAttraction_overviewTtsUpdated() {
        // given
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .latitude(new BigDecimal("33.36160800"))
                .longitude(new BigDecimal("126.53390800"))
                .overview("기존 개요")
                .overviewTts("기존 TTS")
                .build());
        AttractionUpdateServiceRequest request = AttractionUpdateServiceRequest.of(
                null, null, null, null, null, "새로운 TTS");

        // when
        attractionService.updateAttraction(attraction.getId(), request);

        // then
        Attraction updated = attractionRepository.findById(attraction.getId()).get();
        assertThat(updated.getOverviewTts()).isEqualTo("새로운 TTS");
        assertThat(updated.getOverview()).isEqualTo("기존 개요");
        assertThat(updated.getTitle()).isEqualTo("한라산");
    }

    @DisplayName("존재하지 않는 관광지 ID로 수정하면 ResourceNotFoundException이 발생한다.")
    @Test
    void updateAttraction_throwsExceptionWhenNotFound() {
        // given
        AttractionUpdateServiceRequest request = AttractionUpdateServiceRequest.of(
                "한라산", null, null, null, null, null);

        // when & then
        assertThatThrownBy(() -> attractionService.updateAttraction(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Attraction buildAttraction(String title, String overviewTts) {
        return Attraction.builder()
                .contentId(12345)
                .title(title)
                .addr1("서울특별시 용산구")
                .latitude(BigDecimal.valueOf(37.5511))
                .longitude(BigDecimal.valueOf(126.9882))
                .overview("Overview text")
                .overviewTts(overviewTts)
                .build();
    }
}
