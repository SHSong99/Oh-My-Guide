package com.e103.ohmyguide.domain.phrase.controller;

import com.e103.ohmyguide.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PhraseControllerTest extends ControllerTestSupport {

    @DisplayName("POST /phrases/{phraseId}/bookmark - 북마크 추가 성공 시 200을 반환한다.")
    @Test
    void addBookmark_returns200() throws Exception {
        // given
        Long phraseId = 1L;
        willDoNothing().given(phraseService).addBookmark(eq(phraseId), isNull());

        // when & then
        mockMvc.perform(post("/phrases/{phraseId}/bookmark", phraseId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("POST /phrases/{phraseId}/bookmark - 서비스를 정확히 한 번 호출한다.")
    @Test
    void addBookmark_callsServiceOnce() throws Exception {
        // given
        Long phraseId = 1L;
        willDoNothing().given(phraseService).addBookmark(eq(phraseId), isNull());

        // when
        mockMvc.perform(post("/phrases/{phraseId}/bookmark", phraseId));

        // then
        then(phraseService).should(times(1)).addBookmark(eq(phraseId), isNull());
    }

    @DisplayName("DELETE /phrases/{phraseId}/bookmark - 북마크 삭제 성공 시 204를 반환한다.")
    @Test
    void removeBookmark_returns204() throws Exception {
        // given
        Long phraseId = 1L;
        willDoNothing().given(phraseService).removeBookmark(eq(phraseId), isNull());

        // when & then
        mockMvc.perform(delete("/phrases/{phraseId}/bookmark", phraseId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @DisplayName("DELETE /phrases/{phraseId}/bookmark - 서비스를 정확히 한 번 호출한다.")
    @Test
    void removeBookmark_callsServiceOnce() throws Exception {
        // given
        Long phraseId = 1L;
        willDoNothing().given(phraseService).removeBookmark(eq(phraseId), isNull());

        // when
        mockMvc.perform(delete("/phrases/{phraseId}/bookmark", phraseId));

        // then
        then(phraseService).should(times(1)).removeBookmark(eq(phraseId), isNull());
    }

    @DisplayName("POST /phrases/{phraseId}/bookmark - 잘못된 경로 형식으로 요청 시 400을 반환한다.")
    @Test
    void addBookmark_invalidPathVariable_returns400() throws Exception {
        // when & then
        mockMvc.perform(post("/phrases/invalid/bookmark"))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("DELETE /phrases/{phraseId}/bookmark - 잘못된 경로 형식으로 요청 시 400을 반환한다.")
    @Test
    void removeBookmark_invalidPathVariable_returns400() throws Exception {
        // when & then
        mockMvc.perform(delete("/phrases/invalid/bookmark"))
                .andExpect(status().isBadRequest());
    }
}
