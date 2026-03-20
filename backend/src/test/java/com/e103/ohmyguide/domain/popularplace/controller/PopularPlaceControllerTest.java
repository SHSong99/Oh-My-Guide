package com.e103.ohmyguide.domain.popularplace.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.domain.popularplace.dto.PopularPlaceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PopularPlaceControllerTest extends ControllerTestSupport {

    @DisplayName("GET /pickRecommend - 군집 조건으로 인기 장소 추천 목록을 반환한다.")
    @Test
    void getRecommendations_returns200() throws Exception {
        // given
        List<PopularPlaceResponse> response = List.of(
                PopularPlaceResponse.builder().placeId(100L).visitCount(50L).totalScore(200L).placeRank(1).build(),
                PopularPlaceResponse.builder().placeId(200L).visitCount(30L).totalScore(120L).placeRank(2).build()
        );

        // stubbing
        given(popularPlaceService.getRecommendations("KOR", "20s", "M", "LEISURE", "ACTIVE"))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/pickRecommend")
                        .param("nationality", "KOR")
                        .param("ageGroup", "20s")
                        .param("gender", "M")
                        .param("travelPurpose", "LEISURE")
                        .param("lifestyle", "ACTIVE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].placeId").value(100))
                .andExpect(jsonPath("$[0].visitCount").value(50))
                .andExpect(jsonPath("$[0].totalScore").value(200))
                .andExpect(jsonPath("$[0].placeRank").value(1))
                .andExpect(jsonPath("$[1].placeId").value(200))
                .andExpect(jsonPath("$[1].placeRank").value(2));
    }

    @DisplayName("GET /pickRecommend - 결과가 없으면 빈 배열을 반환한다.")
    @Test
    void getRecommendations_returnsEmptyList() throws Exception {
        // given
        given(popularPlaceService.getRecommendations("KOR", "20s", "M", "LEISURE", "ACTIVE"))
                .willReturn(List.of());

        // when & then
        mockMvc.perform(get("/pickRecommend")
                        .param("nationality", "KOR")
                        .param("ageGroup", "20s")
                        .param("gender", "M")
                        .param("travelPurpose", "LEISURE")
                        .param("lifestyle", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @DisplayName("GET /pickRecommend - 필수 파라미터가 누락되면 400을 반환한다.")
    @Test
    void getRecommendations_missingParam_returns400() throws Exception {
        mockMvc.perform(get("/pickRecommend")
                        .param("nationality", "KOR")
                        // ageGroup 누락
                        .param("gender", "M")
                        .param("travelPurpose", "LEISURE")
                        .param("lifestyle", "ACTIVE"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /pickRecommend - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getRecommendations_callsServiceOnce() throws Exception {
        // given
        given(popularPlaceService.getRecommendations("KOR", "20s", "M", "LEISURE", "ACTIVE"))
                .willReturn(List.of());

        // when
        mockMvc.perform(get("/pickRecommend")
                .param("nationality", "KOR")
                .param("ageGroup", "20s")
                .param("gender", "M")
                .param("travelPurpose", "LEISURE")
                .param("lifestyle", "ACTIVE"));

        // then
        then(popularPlaceService).should(times(1))
                .getRecommendations("KOR", "20s", "M", "LEISURE", "ACTIVE");
    }

    @DisplayName("POST /pickRecommend/calculate - Spark 작업 제출 성공 시 202를 반환한다.")
    @Test
    void triggerSparkAnalysis_returns202() throws Exception {
        // given
        given(sparkJobService.submitAnalysisJob())
                .willReturn(Map.of("status", "submitted"));

        // when & then
        mockMvc.perform(post("/pickRecommend/calculate"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("submitted"));
    }

    @DisplayName("POST /pickRecommend/calculate - Spark 작업 제출 실패 시 500을 반환한다.")
    @Test
    void triggerSparkAnalysis_returns500() throws Exception {
        // given
        given(sparkJobService.submitAnalysisJob())
                .willReturn(Map.of("status", "failed", "error", "connection refused"));

        // when & then
        mockMvc.perform(post("/pickRecommend/calculate"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("failed"))
                .andExpect(jsonPath("$.error").value("connection refused"));
    }
}
