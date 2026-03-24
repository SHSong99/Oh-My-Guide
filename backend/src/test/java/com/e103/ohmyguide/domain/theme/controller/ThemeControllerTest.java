package com.e103.ohmyguide.domain.theme.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
