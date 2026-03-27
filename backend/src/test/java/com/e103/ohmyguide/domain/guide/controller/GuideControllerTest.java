package com.e103.ohmyguide.domain.guide.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.guide.dto.GuideGoResponse;
import com.e103.ohmyguide.domain.guide.dto.GuideResponse;
import com.e103.ohmyguide.domain.guide.dto.StartLocationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GuideControllerTest extends ControllerTestSupport {

    private UsernamePasswordAuthenticationToken mockAuth() {
        UserPrincipal principal = new UserPrincipal(
                1L, "test@test.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @DisplayName("GET /guide/{placeId} - 출발지, 목적지 정보를 200과 함께 반환한다.")
    @Test
    void startNavigation_returns200() throws Exception {
        // given
        GuideGoResponse response = GuideGoResponse.builder()
                .startLocation(StartLocationResponse.builder()
                        .latitude(new BigDecimal("35.15"))
                        .longitude(new BigDecimal("129.16"))
                        .build())
                .destination(GuideResponse.builder()
                        .placeId(123L)
                        .title("해운대 해수욕장")
                        .addr1("부산광역시 해운대구 해운대해변로 264")
                        .latitude(new BigDecimal("35.15887"))
                        .longitude(new BigDecimal("129.16044"))
                        .firstImage1("https://example.com/image.jpg")
                        .overview("해운대 해수욕장은 부산을 대표하는 해수욕장이다.")
                        .overviewTts("해운대 해수욕장은 부산을 대표하는 해수욕장입니다.")
                        .build())
                .build();

        given(guideService.startNavigation(any(), any(), any(), any(), any(), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/guide/123")
                        .with(authentication(mockAuth()))
                        .param("currentLat", "35.15")
                        .param("currentLng", "129.16")
                        .param("reachLat", "35.10")
                        .param("reachLng", "129.03"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startLocation.latitude").value(35.15))
                .andExpect(jsonPath("$.startLocation.longitude").value(129.16))
                .andExpect(jsonPath("$.destination.placeId").value(123))
                .andExpect(jsonPath("$.destination.title").value("해운대 해수욕장"))
                .andExpect(jsonPath("$.destination.overview").value("해운대 해수욕장은 부산을 대표하는 해수욕장이다."))
                .andExpect(jsonPath("$.destination.overviewTts").value("해운대 해수욕장은 부산을 대표하는 해수욕장입니다."));
    }

    @DisplayName("GET /guide/{placeId} - 최소 정보로도 200을 반환한다.")
    @Test
    void startNavigation_minimal_returns200() throws Exception {
        // given
        GuideGoResponse response = GuideGoResponse.builder()
                .startLocation(StartLocationResponse.builder()
                        .latitude(new BigDecimal("35.15"))
                        .longitude(new BigDecimal("129.16"))
                        .build())
                .destination(GuideResponse.builder().placeId(123L).build())
                .build();

        given(guideService.startNavigation(any(), any(), any(), any(), any(), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(get("/guide/123")
                        .with(authentication(mockAuth()))
                        .param("currentLat", "35.15")
                        .param("currentLng", "129.16")
                        .param("reachLat", "35.10")
                        .param("reachLng", "129.03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination.placeId").value(123));
    }

    @DisplayName("GET /guide/{placeId} - 필수 좌표 파라미터 누락 시 400을 반환한다.")
    @Test
    void startNavigation_missingParam_returns400() throws Exception {
        // when & then - currentLat 누락
        mockMvc.perform(get("/guide/123")
                        .with(authentication(mockAuth()))
                        .param("currentLng", "129.16")
                        .param("reachLat", "35.10")
                        .param("reachLng", "129.03"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("GET /guide/sse - SSE 스트림 연결에 성공하면 200을 반환한다.")
    @Test
    void connectSse_returns200() throws Exception {
        // given
        given(sseEmitterManager.create(any())).willReturn(new SseEmitter());

        // when & then
        mockMvc.perform(get("/guide/sse")
                        .with(authentication(mockAuth()))
                        .accept("text/event-stream"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
