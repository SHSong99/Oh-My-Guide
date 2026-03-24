package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
import com.e103.ohmyguide.domain.theme.response.AttractionSummaryResponse;
import com.e103.ohmyguide.domain.theme.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import com.e103.ohmyguide.domain.themeattraction.repository.ThemeAttractionRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeServiceTest extends IntegrationTestSupport {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private ThemeAttractionRepository themeAttractionRepository;

    @DisplayName("저장된 모든 테마를 count와 함께 반환한다.")
    @Test
    void getThemes_returnsAllThemesWithCount() {
        // given
        themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        themeRepository.save(Theme.builder().name("역사").description("역사 유적 테마").build());

        // when
        ThemeInfosResponse result = themeService.getThemes();

        // then
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getThemes()).hasSize(2);
        assertThat(result.getThemes())
                .extracting(ThemeInfoResponse::getName)
                .containsExactlyInAnyOrder("자연", "역사");
    }

    @DisplayName("테마가 없으면 count 0과 빈 리스트를 반환한다.")
    @Test
    void getThemes_returnsEmptyWithCountZero() {
        // given (저장 없음)

        // when
        ThemeInfosResponse result = themeService.getThemes();

        // then
        assertThat(result.getCount()).isZero();
        assertThat(result.getThemes()).isEmpty();
    }

    @DisplayName("테마의 themeId, name, description이 DTO에 올바르게 매핑된다.")
    @Test
    void getThemes_entityMappedCorrectlyToDto() {
        // given
        Theme saved = themeRepository.save(Theme.builder().name("문화").description("문화 예술 테마").build());

        // when
        ThemeInfosResponse result = themeService.getThemes();

        // then
        assertThat(result.getCount()).isEqualTo(1);
        ThemeInfoResponse response = result.getThemes().get(0);
        assertThat(response.getThemeId()).isEqualTo(saved.getId());
        assertThat(response.getName()).isEqualTo("문화");
        assertThat(response.getDescription()).isEqualTo("문화 예술 테마");
    }

    @DisplayName("테마 ID로 조회하면 테마 정보와 관광지 목록을 반환한다.")
    @Test
    void getTheme_returnsThemeDetailWithAttractions() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .firstImage1("image_url")
                .overview("한라산 개요")
                .build());
        ThemeAttraction ta = ThemeAttraction.builder().build();
        ta.assignTheme(theme);
        ta.assignAttraction(attraction);
        themeAttractionRepository.save(ta);

        // when
        ThemeDetailResponse result = themeService.getTheme(theme.getId());

        // then
        assertThat(result.getThemeId()).isEqualTo(theme.getId());
        assertThat(result.getName()).isEqualTo("자연");
        assertThat(result.getDescription()).isEqualTo("자연 경관 테마");
        assertThat(result.getAttractionCount()).isEqualTo(1);
        assertThat(result.getAttractions()).hasSize(1);
        AttractionSummaryResponse attractionResponse = result.getAttractions().get(0);
        assertThat(attractionResponse.getAttractionId()).isEqualTo(attraction.getId());
        assertThat(attractionResponse.getTitle()).isEqualTo("한라산");
        assertThat(attractionResponse.getImage()).isEqualTo("image_url");
        assertThat(attractionResponse.getOverview()).isEqualTo("한라산 개요");
    }

    @DisplayName("테마에 관광지가 없으면 빈 리스트와 count 0을 반환한다.")
    @Test
    void getTheme_returnsEmptyAttractionsWhenNone() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("역사").description("역사 유적 테마").build());

        // when
        ThemeDetailResponse result = themeService.getTheme(theme.getId());

        // then
        assertThat(result.getAttractionCount()).isZero();
        assertThat(result.getAttractions()).isEmpty();
    }

    @DisplayName("존재하지 않는 테마 ID로 조회하면 ResourceNotFoundException이 발생한다.")
    @Test
    void getTheme_throwsExceptionWhenNotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> themeService.getTheme(nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
