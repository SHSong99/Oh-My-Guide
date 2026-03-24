package com.e103.ohmyguide.domain.theme.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.domain.theme.service.response.AttractionSummaryResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThemeControllerTest extends ControllerTestSupport {

    @DisplayName("GET /api/themes - 모든 테마 목록을 count와 함께 반환한다.")
    @Test
    void getThemes_returns200() throws Exception {
        // given
        List<ThemeInfoResponse> themes = List.of(
                ThemeInfoResponse.builder().themeId(1L).name("자연").description("자연 경관 테마").build(),
                ThemeInfoResponse.builder().themeId(2L).name("역사").description("역사 유적 테마").build()
        );
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(themes));

        // when & then
        mockMvc.perform(get("/api/themes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.themes.length()").value(2))
                .andExpect(jsonPath("$.themes[0].themeId").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("자연"))
                .andExpect(jsonPath("$.themes[0].description").value("자연 경관 테마"))
                .andExpect(jsonPath("$.themes[1].themeId").value(2))
                .andExpect(jsonPath("$.themes[1].name").value("역사"));
    }

    @DisplayName("GET /api/themes - 테마가 없으면 count 0과 빈 리스트를 반환한다.")
    @Test
    void getThemes_returnsEmptyList() throws Exception {
        // given
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(List.of()));

        // when & then
        mockMvc.perform(get("/api/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.themes").isEmpty());
    }

    @DisplayName("GET /api/themes - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getThemes_callsServiceOnce() throws Exception {
        // given
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(List.of()));

        // when
        mockMvc.perform(get("/api/themes"));

        // then
        then(themeService).should(times(1)).getThemes();
    }

    @DisplayName("GET /api/themes/{themeId} - 테마 상세 정보와 관광지 목록을 반환한다.")
    @Test
    void getTheme_returns200() throws Exception {
        // given
        List<AttractionSummaryResponse> attractions = List.of(
                AttractionSummaryResponse.builder().attractionId(1L).title("한라산").image("image_url").overview("한라산 개요")
                        .latitude(new BigDecimal("33.36160800")).longitude(new BigDecimal("126.53390800")).attractionOrder(1).build(),
                AttractionSummaryResponse.builder().attractionId(2L).title("성산일출봉").image("image_url2").overview("성산 개요")
                        .latitude(new BigDecimal("33.45840800")).longitude(new BigDecimal("126.94240800")).attractionOrder(2).build()
        );
        ThemeDetailResponse response = ThemeDetailResponse.builder()
                .themeId(1L)
                .name("자연")
                .description("자연 경관 테마")
                .attractionCount(2)
                .attractions(attractions)
                .build();
        given(themeService.getTheme(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/themes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themeId").value(1))
                .andExpect(jsonPath("$.name").value("자연"))
                .andExpect(jsonPath("$.description").value("자연 경관 테마"))
                .andExpect(jsonPath("$.attractionCount").value(2))
                .andExpect(jsonPath("$.attractions.length()").value(2))
                .andExpect(jsonPath("$.attractions[0].attractionId").value(1))
                .andExpect(jsonPath("$.attractions[0].title").value("한라산"))
                .andExpect(jsonPath("$.attractions[0].latitude").value(33.36160800))
                .andExpect(jsonPath("$.attractions[0].longitude").value(126.53390800))
                .andExpect(jsonPath("$.attractions[0].attractionOrder").value(1))
                .andExpect(jsonPath("$.attractions[1].attractionId").value(2))
                .andExpect(jsonPath("$.attractions[1].title").value("성산일출봉"))
                .andExpect(jsonPath("$.attractions[1].latitude").value(33.45840800))
                .andExpect(jsonPath("$.attractions[1].longitude").value(126.94240800))
                .andExpect(jsonPath("$.attractions[1].attractionOrder").value(2));
    }

    @DisplayName("GET /api/themes/{themeId} - 존재하지 않는 테마 ID 조회 시 404를 반환한다.")
    @Test
    void getTheme_returns404WhenNotFound() throws Exception {
        // given
        given(themeService.getTheme(999L))
                .willThrow(new ResourceNotFoundException("Theme", "themeId", 999L));

        // when & then
        mockMvc.perform(get("/api/themes/999"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("GET /api/themes/{themeId} - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getTheme_callsServiceOnce() throws Exception {
        // given
        given(themeService.getTheme(1L))
                .willReturn(ThemeDetailResponse.builder()
                        .themeId(1L).name("자연").description("자연 경관 테마")
                        .attractionCount(0).attractions(List.of())
                        .build());

        // when
        mockMvc.perform(get("/api/themes/1"));

        // then
        then(themeService).should(times(1)).getTheme(1L);
    }
}
