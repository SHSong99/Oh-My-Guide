package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
import com.e103.ohmyguide.domain.theme.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.response.ThemeInfosResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeServiceTest extends IntegrationTestSupport {

    @Autowired
    private ThemeService themeService;

    @Autowired
    private ThemeRepository themeRepository;

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
}
