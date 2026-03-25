package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import java.math.BigDecimal;
import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
import com.e103.ohmyguide.domain.theme.service.request.ThemeAttractionAddServiceRequest;
import com.e103.ohmyguide.domain.theme.service.request.ThemeCreateServiceRequest;
import com.e103.ohmyguide.domain.theme.service.request.ThemeUpdateServiceRequest;

import com.e103.ohmyguide.domain.theme.service.response.AttractionSummaryResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import com.e103.ohmyguide.domain.themeattraction.repository.ThemeAttractionRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;

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

    @DisplayName("테마를 생성하면 DB에 저장된다.")
    @Test
    void createTheme_savedSuccessfully() {
        // given
        ThemeCreateServiceRequest request = ThemeCreateServiceRequest.of("자연", "자연 경관 테마", null, null);

        // when
        themeService.createTheme(request);

        // then
        assertThat(themeRepository.findAll()).hasSize(1);
        Theme saved = themeRepository.findAll().get(0);
        assertThat(saved.getName()).isEqualTo("자연");
        assertThat(saved.getDescription()).isEqualTo("자연 경관 테마");
    }

    @DisplayName("테마 name과 description을 수정하면 DB에 반영된다.")
    @Test
    void updateTheme_updatedSuccessfully() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        ThemeUpdateServiceRequest request = ThemeUpdateServiceRequest.of("역사", "역사 유적 테마", null, null);

        // when
        themeService.updateTheme(theme.getId(), request);

        // then
        Theme updated = themeRepository.findById(theme.getId()).get();
        assertThat(updated.getName()).isEqualTo("역사");
        assertThat(updated.getDescription()).isEqualTo("역사 유적 테마");
    }

    @DisplayName("존재하지 않는 테마 ID로 수정하면 ResourceNotFoundException이 발생한다.")
    @Test
    void updateTheme_throwsExceptionWhenNotFound() {
        // given
        ThemeUpdateServiceRequest request = ThemeUpdateServiceRequest.of("역사", "역사 유적 테마", null, null);

        // when & then
        assertThatThrownBy(() -> themeService.updateTheme(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("테마를 삭제하면 DB에서 제거된다.")
    @Test
    void deleteTheme_deletedSuccessfully() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());

        // when
        themeService.deleteTheme(theme.getId());

        // then
        assertThat(themeRepository.findById(theme.getId())).isEmpty();
    }

    @DisplayName("존재하지 않는 테마 ID로 삭제하면 ResourceNotFoundException이 발생한다.")
    @Test
    void deleteTheme_throwsExceptionWhenNotFound() {
        // when & then
        assertThatThrownBy(() -> themeService.deleteTheme(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("테마에 관광지를 추가하면 ThemeAttraction이 저장된다.")
    @Test
    void addAttraction_savedSuccessfully() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        Attraction attraction = attractionRepository.save(Attraction.builder().title("한라산").build());
        ThemeAttractionAddServiceRequest request = ThemeAttractionAddServiceRequest.of(attraction.getId(), 1);

        // when
        themeService.addAttraction(theme.getId(), request);

        // then
        assertThat(themeAttractionRepository.findAll()).hasSize(1);
        ThemeAttraction saved = themeAttractionRepository.findAll().get(0);
        assertThat(saved.getAttractionOrder()).isEqualTo(1);
    }

    @DisplayName("존재하지 않는 테마에 관광지를 추가하면 ResourceNotFoundException이 발생한다.")
    @Test
    void addAttraction_throwsExceptionWhenThemeNotFound() {
        // given
        Attraction attraction = attractionRepository.save(Attraction.builder().title("한라산").build());
        ThemeAttractionAddServiceRequest request = ThemeAttractionAddServiceRequest.of(attraction.getId(), 1);

        // when & then
        assertThatThrownBy(() -> themeService.addAttraction(999L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("존재하지 않는 관광지를 테마에 추가하면 ResourceNotFoundException이 발생한다.")
    @Test
    void addAttraction_throwsExceptionWhenAttractionNotFound() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        ThemeAttractionAddServiceRequest request = ThemeAttractionAddServiceRequest.of(999L, 1);

        // when & then
        assertThatThrownBy(() -> themeService.addAttraction(theme.getId(), request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("테마에서 관광지를 제거하면 ThemeAttraction이 삭제된다.")
    @Test
    void removeAttraction_deletedSuccessfully() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        Attraction attraction = attractionRepository.save(Attraction.builder().title("한라산").build());
        ThemeAttraction ta = ThemeAttraction.builder().attractionOrder(1).build();
        ta.assignTheme(theme);
        ta.assignAttraction(attraction);
        ThemeAttraction saved = themeAttractionRepository.save(ta);

        // when
        themeService.removeAttraction(theme.getId(), attraction.getId());

        // then
        assertThat(themeAttractionRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("존재하지 않는 테마로 관광지를 제거하면 ResourceNotFoundException이 발생한다.")
    @Test
    void removeAttraction_throwsExceptionWhenThemeNotFound() {
        // when & then
        assertThatThrownBy(() -> themeService.removeAttraction(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("테마에 속하지 않는 attractionId로 제거하면 ResourceNotFoundException이 발생한다.")
    @Test
    void removeAttraction_throwsExceptionWhenAttractionNotInTheme() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());

        // when & then
        assertThatThrownBy(() -> themeService.removeAttraction(theme.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

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

    @DisplayName("테마의 themeId, name, description, category, region이 DTO에 올바르게 매핑된다.")
    @Test
    void getThemes_entityMappedCorrectlyToDto() {
        // given
        Theme saved = themeRepository.save(Theme.builder().name("문화").description("문화 예술 테마").category("역사/문화").region("서울").build());

        // when
        ThemeInfosResponse result = themeService.getThemes();

        // then
        assertThat(result.getCount()).isEqualTo(1);
        ThemeInfoResponse response = result.getThemes().get(0);
        assertThat(response.getThemeId()).isEqualTo(saved.getId());
        assertThat(response.getName()).isEqualTo("문화");
        assertThat(response.getDescription()).isEqualTo("문화 예술 테마");
        assertThat(response.getCategory()).isEqualTo("역사/문화");
        assertThat(response.getRegion()).isEqualTo("서울");
    }

    @DisplayName("테마 ID로 조회하면 테마 정보와 관광지 목록을 반환한다.")
    @Test
    void getTheme_returnsThemeDetailWithAttractions() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").category("자연/생태").region("제주").build());
        Attraction attraction = attractionRepository.save(Attraction.builder()
                .title("한라산")
                .firstImage1("image_url")
                .overview("한라산 개요")
                .latitude(new BigDecimal("33.36160800"))
                .longitude(new BigDecimal("126.53390800"))
                .build());
        ThemeAttraction ta = ThemeAttraction.builder().attractionOrder(1).build();
        ta.assignTheme(theme);
        ta.assignAttraction(attraction);
        themeAttractionRepository.save(ta);

        // when
        ThemeDetailResponse result = themeService.getTheme(theme.getId());

        // then
        assertThat(result.getThemeId()).isEqualTo(theme.getId());
        assertThat(result.getName()).isEqualTo("자연");
        assertThat(result.getDescription()).isEqualTo("자연 경관 테마");
        assertThat(result.getCategory()).isEqualTo("자연/생태");
        assertThat(result.getRegion()).isEqualTo("제주");
        assertThat(result.getAttractionCount()).isEqualTo(1);
        assertThat(result.getAttractions()).hasSize(1);
        AttractionSummaryResponse attractionResponse = result.getAttractions().get(0);
        assertThat(attractionResponse.getAttractionId()).isEqualTo(attraction.getId());
        assertThat(attractionResponse.getTitle()).isEqualTo("한라산");
        assertThat(attractionResponse.getImage()).isEqualTo("image_url");
        assertThat(attractionResponse.getOverview()).isEqualTo("한라산 개요");
        assertThat(attractionResponse.getOverviewTts()).isNull();
        assertThat(attractionResponse.getLatitude()).isEqualByComparingTo(new BigDecimal("33.36160800"));
        assertThat(attractionResponse.getLongitude()).isEqualByComparingTo(new BigDecimal("126.53390800"));
        assertThat(attractionResponse.getAttractionOrder()).isEqualTo(1);
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

    @DisplayName("관광지가 attractionOrder 오름차순으로 정렬되어 반환된다.")
    @Test
    void getTheme_returnsAttractionsSortedByOrder() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        Attraction first = attractionRepository.save(Attraction.builder().title("한라산").build());
        Attraction second = attractionRepository.save(Attraction.builder().title("성산일출봉").build());
        Attraction third = attractionRepository.save(Attraction.builder().title("천지연폭포").build());

        ThemeAttraction ta2 = ThemeAttraction.builder().attractionOrder(2).build();
        ta2.assignTheme(theme);
        ta2.assignAttraction(second);
        themeAttractionRepository.save(ta2);

        ThemeAttraction ta3 = ThemeAttraction.builder().attractionOrder(3).build();
        ta3.assignTheme(theme);
        ta3.assignAttraction(third);
        themeAttractionRepository.save(ta3);

        ThemeAttraction ta1 = ThemeAttraction.builder().attractionOrder(1).build();
        ta1.assignTheme(theme);
        ta1.assignAttraction(first);
        themeAttractionRepository.save(ta1);

        // when
        ThemeDetailResponse result = themeService.getTheme(theme.getId());

        // then
        assertThat(result.getAttractions())
                .extracting(AttractionSummaryResponse::getTitle)
                .containsExactly("한라산", "성산일출봉", "천지연폭포");
    }

    @DisplayName("같은 테마 내에서 동일한 attractionOrder로 저장하면 예외가 발생한다.")
    @Test
    void saveThemeAttraction_throwsExceptionOnDuplicateOrder() {
        // given
        Theme theme = themeRepository.save(Theme.builder().name("자연").description("자연 경관 테마").build());
        Attraction attraction1 = attractionRepository.save(Attraction.builder().title("한라산").build());
        Attraction attraction2 = attractionRepository.save(Attraction.builder().title("성산일출봉").build());

        ThemeAttraction ta1 = ThemeAttraction.builder().attractionOrder(1).build();
        ta1.assignTheme(theme);
        ta1.assignAttraction(attraction1);
        themeAttractionRepository.save(ta1);

        // when & then
        ThemeAttraction ta2 = ThemeAttraction.builder().attractionOrder(1).build();
        ta2.assignTheme(theme);
        ta2.assignAttraction(attraction2);

        assertThatThrownBy(() -> themeAttractionRepository.saveAndFlush(ta2))
                .isInstanceOf(DataIntegrityViolationException.class);
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
