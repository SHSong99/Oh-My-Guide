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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ThemeControllerTest extends ControllerTestSupport {

    @DisplayName("GET /themes - 모든 테마 목록을 count와 함께 반환한다.")
    @Test
    void getThemes_returns200() throws Exception {
        // given
        List<ThemeInfoResponse> themes = List.of(
                ThemeInfoResponse.builder().themeId(1L).name("자연").description("자연 경관 테마").category("자연/생태").region("제주").build(),
                ThemeInfoResponse.builder().themeId(2L).name("역사").description("역사 유적 테마").category("역사/문화").region("경주").build()
        );
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(themes));

        // when & then
        mockMvc.perform(get("/themes"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.themes.length()").value(2))
                .andExpect(jsonPath("$.themes[0].themeId").value(1))
                .andExpect(jsonPath("$.themes[0].name").value("자연"))
                .andExpect(jsonPath("$.themes[0].description").value("자연 경관 테마"))
                .andExpect(jsonPath("$.themes[0].category").value("자연/생태"))
                .andExpect(jsonPath("$.themes[0].region").value("제주"))
                .andExpect(jsonPath("$.themes[1].themeId").value(2))
                .andExpect(jsonPath("$.themes[1].name").value("역사"))
                .andExpect(jsonPath("$.themes[1].category").value("역사/문화"))
                .andExpect(jsonPath("$.themes[1].region").value("경주"));
    }

    @DisplayName("GET /themes - 테마가 없으면 count 0과 빈 리스트를 반환한다.")
    @Test
    void getThemes_returnsEmptyList() throws Exception {
        // given
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(List.of()));

        // when & then
        mockMvc.perform(get("/themes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.themes").isEmpty());
    }

    @DisplayName("GET /themes - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getThemes_callsServiceOnce() throws Exception {
        // given
        given(themeService.getThemes())
                .willReturn(ThemeInfosResponse.of(List.of()));

        // when
        mockMvc.perform(get("/themes"));

        // then
        then(themeService).should(times(1)).getThemes();
    }

    @DisplayName("GET /themes/{themeId} - 테마 상세 정보와 관광지 목록을 반환한다.")
    @Test
    void getTheme_returns200() throws Exception {
        // given
        List<AttractionSummaryResponse> attractions = List.of(
                AttractionSummaryResponse.builder().attractionId(1L).title("한라산").image("image_url").overview("한라산 개요").overviewTts("한라산 TTS")
                        .latitude(new BigDecimal("33.36160800")).longitude(new BigDecimal("126.53390800")).attractionOrder(1).build(),
                AttractionSummaryResponse.builder().attractionId(2L).title("성산일출봉").image("image_url2").overview("성산 개요").overviewTts("성산 TTS")
                        .latitude(new BigDecimal("33.45840800")).longitude(new BigDecimal("126.94240800")).attractionOrder(2).build()
        );
        ThemeDetailResponse response = ThemeDetailResponse.builder()
                .themeId(1L)
                .name("자연")
                .description("자연 경관 테마")
                .category("자연/생태")
                .region("제주")
                .attractionCount(2)
                .attractions(attractions)
                .build();
        given(themeService.getTheme(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/themes/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.themeId").value(1))
                .andExpect(jsonPath("$.name").value("자연"))
                .andExpect(jsonPath("$.description").value("자연 경관 테마"))
                .andExpect(jsonPath("$.category").value("자연/생태"))
                .andExpect(jsonPath("$.region").value("제주"))
                .andExpect(jsonPath("$.attractionCount").value(2))
                .andExpect(jsonPath("$.attractions.length()").value(2))
                .andExpect(jsonPath("$.attractions[0].attractionId").value(1))
                .andExpect(jsonPath("$.attractions[0].title").value("한라산"))
                .andExpect(jsonPath("$.attractions[0].overview").value("한라산 개요"))
                .andExpect(jsonPath("$.attractions[0].overviewTts").value("한라산 TTS"))
                .andExpect(jsonPath("$.attractions[0].latitude").value(33.36160800))
                .andExpect(jsonPath("$.attractions[0].longitude").value(126.53390800))
                .andExpect(jsonPath("$.attractions[0].attractionOrder").value(1))
                .andExpect(jsonPath("$.attractions[1].attractionId").value(2))
                .andExpect(jsonPath("$.attractions[1].title").value("성산일출봉"))
                .andExpect(jsonPath("$.attractions[1].overviewTts").value("성산 TTS"))
                .andExpect(jsonPath("$.attractions[1].latitude").value(33.45840800))
                .andExpect(jsonPath("$.attractions[1].longitude").value(126.94240800))
                .andExpect(jsonPath("$.attractions[1].attractionOrder").value(2));
    }

    @DisplayName("GET /themes/{themeId} - 존재하지 않는 테마 ID 조회 시 404를 반환한다.")
    @Test
    void getTheme_returns404WhenNotFound() throws Exception {
        // given
        given(themeService.getTheme(999L))
                .willThrow(new ResourceNotFoundException("Theme", "themeId", 999L));

        // when & then
        mockMvc.perform(get("/themes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Theme not found with themeId : '999'"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @DisplayName("GET /themes/{themeId} - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getTheme_callsServiceOnce() throws Exception {
        // given
        given(themeService.getTheme(1L))
                .willReturn(ThemeDetailResponse.builder()
                        .themeId(1L).name("자연").description("자연 경관 테마")
                        .category(null).region(null)
                        .attractionCount(0).attractions(List.of())
                        .build());

        // when
        mockMvc.perform(get("/themes/1"));

        // then
        then(themeService).should(times(1)).getTheme(1L);
    }

    @DisplayName("POST /themes - 테마를 생성하면 200을 반환한다.")
    @Test
    void createTheme_returns200() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("name", "자연");
                    put("description", "자연 경관 테마");
                }}
        );

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("POST /themes - name이 비어있으면 400을 반환한다.")
    @Test
    void createTheme_returns400WhenNameBlank() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("name", "");
                    put("description", "자연 경관 테마");
                }}
        );

        // when & then
        mockMvc.perform(post("/themes")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("name"));
    }

    @DisplayName("PUT /themes/{themeId} - 테마를 수정하면 200을 반환한다.")
    @Test
    void updateTheme_returns200() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("name", "역사");
                    put("description", "역사 유적 테마");
                }}
        );

        // when & then
        mockMvc.perform(put("/themes/1")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        then(themeService).should(times(1)).updateTheme(eq(1L), any());
    }

    @DisplayName("PUT /themes/{themeId} - description이 비어있으면 400을 반환한다.")
    @Test
    void updateTheme_returns400WhenDescriptionBlank() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("name", "역사");
                    put("description", "");
                }}
        );

        // when & then
        mockMvc.perform(put("/themes/1")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("description"));
    }

    @DisplayName("DELETE /themes/{themeId} - 테마를 삭제하면 204를 반환한다.")
    @Test
    void deleteTheme_returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/themes/1"))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(themeService).should(times(1)).deleteTheme(1L);
    }

    @DisplayName("DELETE /themes/{themeId} - 존재하지 않는 테마 삭제 시 404를 반환한다.")
    @Test
    void deleteTheme_returns404WhenNotFound() throws Exception {
        // given
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Theme", "themeId", 999L))
                .when(themeService).deleteTheme(999L);

        // when & then
        mockMvc.perform(delete("/themes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Theme not found with themeId : '999'"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @DisplayName("POST /themes/{themeId}/attractions - 테마에 관광지를 추가하면 200을 반환한다.")
    @Test
    void addAttraction_returns200() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("attractionId", 1L);
                    put("attractionOrder", 1);
                }}
        );

        // when & then
        mockMvc.perform(post("/themes/1/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        then(themeService).should(times(1)).addAttraction(eq(1L), any());
    }

    @DisplayName("POST /themes/{themeId}/attractions - attractionId가 없으면 400을 반환한다.")
    @Test
    void addAttraction_returns400WhenAttractionIdNull() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("attractionOrder", 1);
                }}
        );

        // when & then
        mockMvc.perform(post("/themes/1/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("attractionId"));
    }

    @DisplayName("POST /themes/{themeId}/attractions - attractionOrder가 없으면 400을 반환한다.")
    @Test
    void addAttraction_returns400WhenAttractionOrderNull() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("attractionId", 1L);
                }}
        );

        // when & then
        mockMvc.perform(post("/themes/1/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("attractionOrder"));
    }

    @DisplayName("DELETE /themes/{themeId}/attractions/{attractionId} - 관광지를 제거하면 204를 반환한다.")
    @Test
    void removeAttraction_returns204() throws Exception {
        // when & then
        mockMvc.perform(delete("/themes/1/attractions/10"))
                .andDo(print())
                .andExpect(status().isNoContent());

        then(themeService).should(times(1)).removeAttraction(1L, 10L);
    }

    @DisplayName("DELETE /themes/{themeId}/attractions/{attractionId} - 테마에 없는 관광지 제거 시 404를 반환한다.")
    @Test
    void removeAttraction_returns404WhenNotFound() throws Exception {
        // given
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("ThemeAttraction", "attractionId", 999L))
                .when(themeService).removeAttraction(1L, 999L);

        // when & then
        mockMvc.perform(delete("/themes/1/attractions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("ThemeAttraction not found with attractionId : '999'"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
