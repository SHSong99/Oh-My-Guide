package com.e103.ohmyguide.domain.attraction.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @DisplayName("POST /attractions - 관광지를 등록하면 201과 생성된 관광지를 반환한다.")
    @Test
    void createAttraction_returns201() throws Exception {
        // given
        AttractionDetailResponse response = AttractionDetailResponse.builder()
                .attrId(1L)
                .title("한라산")
                .latitude(33.36160800)
                .longitude(126.53390800)
                .firstImage1("http://image.url/hallasan.jpg")
                .overview("한라산 개요")
                .build();
        given(attractionService.createAttraction(any())).willReturn(response);

        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산");
                    put("latitude", new BigDecimal("33.36160800"));
                    put("longitude", new BigDecimal("126.53390800"));
                    put("firstImage1", "http://image.url/hallasan.jpg");
                    put("overview", "한라산 개요");
                    put("overviewTts", "한라산 TTS 개요");
                }}
        );

        // when & then
        mockMvc.perform(post("/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attrId").value(1))
                .andExpect(jsonPath("$.title").value("한라산"))
                .andExpect(jsonPath("$.latitude").value(33.36160800))
                .andExpect(jsonPath("$.longitude").value(126.53390800));
    }

    @DisplayName("POST /attractions - title이 비어있으면 400을 반환한다.")
    @Test
    void createAttraction_returns400WhenTitleBlank() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "");
                    put("latitude", new BigDecimal("33.36160800"));
                    put("longitude", new BigDecimal("126.53390800"));
                }}
        );

        // when & then
        mockMvc.perform(post("/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("title"));
    }

    @DisplayName("POST /attractions - latitude가 없으면 400을 반환한다.")
    @Test
    void createAttraction_returns400WhenLatitudeNull() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산");
                    put("longitude", new BigDecimal("126.53390800"));
                }}
        );

        // when & then
        mockMvc.perform(post("/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("latitude"));
    }

    @DisplayName("POST /attractions - longitude가 없으면 400을 반환한다.")
    @Test
    void createAttraction_returns400WhenLongitudeNull() throws Exception {
        // given
        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산");
                    put("latitude", new BigDecimal("33.36160800"));
                }}
        );

        // when & then
        mockMvc.perform(post("/attractions")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("longitude"));
    }

    @DisplayName("POST /attractions - 서비스를 정확히 한 번 호출한다.")
    @Test
    void createAttraction_callsServiceOnce() throws Exception {
        // given
        given(attractionService.createAttraction(any()))
                .willReturn(AttractionDetailResponse.builder().attrId(1L).title("한라산").build());

        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산");
                    put("latitude", new BigDecimal("33.36160800"));
                    put("longitude", new BigDecimal("126.53390800"));
                }}
        );

        // when
        mockMvc.perform(post("/attractions")
                .contentType(APPLICATION_JSON)
                .content(body));

        // then
        then(attractionService).should(times(1)).createAttraction(any());
    }

    @DisplayName("PATCH /attractions/{attractionId} - 관광지를 수정하면 200과 수정된 관광지를 반환한다.")
    @Test
    void updateAttraction_returns200() throws Exception {
        // given
        AttractionDetailResponse response = AttractionDetailResponse.builder()
                .attrId(1L)
                .title("한라산 국립공원")
                .latitude(33.36160800)
                .longitude(126.53390800)
                .overview("새로운 개요")
                .build();
        given(attractionService.updateAttraction(eq(1L), any())).willReturn(response);

        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산 국립공원");
                    put("overview", "새로운 개요");
                }}
        );

        // when & then
        mockMvc.perform(patch("/attractions/1")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attrId").value(1))
                .andExpect(jsonPath("$.title").value("한라산 국립공원"))
                .andExpect(jsonPath("$.overview").value("새로운 개요"));

        then(attractionService).should(times(1)).updateAttraction(eq(1L), any());
    }

    @DisplayName("PATCH /attractions/{attractionId} - 존재하지 않는 관광지 수정 시 404를 반환한다.")
    @Test
    void updateAttraction_returns404WhenNotFound() throws Exception {
        // given
        given(attractionService.updateAttraction(eq(999L), any()))
                .willThrow(new ResourceNotFoundException("Attraction", "id", 999L));

        String body = objectMapper.writeValueAsString(
                new java.util.HashMap<>() {{
                    put("title", "한라산");
                }}
        );

        // when & then
        mockMvc.perform(patch("/attractions/999")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Attraction not found with id : '999'"))
                .andExpect(jsonPath("$.errors").isEmpty());
    }
}
