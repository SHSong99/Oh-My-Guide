package com.e103.ohmyguide.domain.attraction.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AttractionControllerTest extends ControllerTestSupport {

    @DisplayName("GET /attractions/{attractionId}/guideMessage - 가이드 메시지 조회 성공 시 200을 반환한다.")
    @Test
    void getGuideMessage_returns200() throws Exception {
        // given
        Long attractionId = 1L;
        String guideMessage = "서울타워는 남산에 위치한 관광 명소입니다.";
        given(attractionService.getGuideMessageBy(attractionId)).willReturn(guideMessage);

        // when & then
        mockMvc.perform(get("/attractions/{attractionId}/guideMessage", attractionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guideMessage").value(guideMessage));
    }

    @DisplayName("GET /attractions/{attractionId}/guideMessage - 서비스를 정확히 한 번 호출한다.")
    @Test
    void getGuideMessage_callsServiceOnce() throws Exception {
        // given
        Long attractionId = 1L;
        given(attractionService.getGuideMessageBy(attractionId)).willReturn("가이드 메시지");

        // when
        mockMvc.perform(get("/attractions/{attractionId}/guideMessage", attractionId));

        // then
        then(attractionService).should(times(1)).getGuideMessageBy(attractionId);
    }

    @DisplayName("GET /attractions/{attractionId}/guideMessage - 존재하지 않는 Attraction ID로 요청 시 404를 반환한다.")
    @Test
    void getGuideMessage_attractionNotFound_returns404() throws Exception {
        // given
        Long invalidAttractionId = 999L;
        given(attractionService.getGuideMessageBy(invalidAttractionId))
                .willThrow(new ResourceNotFoundException("Attraction", "id", invalidAttractionId));

        // when & then
        mockMvc.perform(get("/attractions/{attractionId}/guideMessage", invalidAttractionId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("GET /attractions/{attractionId}/guideMessage - overviewTts가 null이면 null을 반환한다.")
    @Test
    void getGuideMessage_overviewTtsIsNull_returnsNull() throws Exception {
        // given
        Long attractionId = 1L;
        given(attractionService.getGuideMessageBy(attractionId)).willReturn(null);

        // when & then
        mockMvc.perform(get("/attractions/{attractionId}/guideMessage", attractionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guideMessage").doesNotExist());
    }

    @DisplayName("GET /attractions/{attractionId}/guideMessage - overviewTts가 빈 문자열이면 빈 문자열을 반환한다.")
    @Test
    void getGuideMessage_overviewTtsIsEmpty_returnsEmpty() throws Exception {
        // given
        Long attractionId = 1L;
        given(attractionService.getGuideMessageBy(attractionId)).willReturn("");

        // when & then
        mockMvc.perform(get("/attractions/{attractionId}/guideMessage", attractionId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guideMessage").value(""));
    }

    @DisplayName("GET /attractions/{attractionId}/guideMessage - 잘못된 경로 형식으로 요청 시 400을 반환한다.")
    @Test
    void getGuideMessage_invalidPathVariable_returns400() throws Exception {
        // when & then
        mockMvc.perform(get("/attractions/invalid/guideMessage"))
                .andExpect(status().isBadRequest());
    }
}
