package com.e103.ohmyguide.domain.guide.consumer;

import com.e103.ohmyguide.domain.guide.consumer.HdfsLog.HdfsLogConsumer;
import com.e103.ohmyguide.domain.guide.consumer.HdfsLog.HdfsLogWriter;
import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HdfsLogConsumerTest {

    @InjectMocks
    private HdfsLogConsumer consumer;

    @Mock
    private HdfsLogWriter hdfsLogWriter;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("메시지를 버퍼에 쌓고 임계치(10개) 미만이면 HDFS에 쓰지 않는다")
    @Test
    void consume_belowThreshold_doesNotFlush() throws Exception {
        // given & when - 9개 메시지 전송
        for (int i = 0; i < 9; i++) {
            consumer.consume(createGoLogJson(1L, (long) (100 + i)));
        }

        // then
        verify(hdfsLogWriter, never()).writeLogs(any());
    }

    @DisplayName("메시지가 10개(임계치) 도달 시 버퍼를 HDFS로 flush한다")
    @Test
    void consume_reachesThreshold_flushesToHdfs() throws Exception {
        // given & when - 10개 메시지 전송
        for (int i = 0; i < 10; i++) {
            consumer.consume(createGoLogJson(1L, (long) (100 + i)));
        }

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserGoLogMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(hdfsLogWriter, times(1)).writeLogs(captor.capture());

        List<UserGoLogMessage> flushedLogs = captor.getValue();
        assertThat(flushedLogs).hasSize(10);
        assertThat(flushedLogs.get(0).getAction()).isEqualTo("GO");
        assertThat(flushedLogs.get(0).getPlaceId()).isEqualTo(100L);
    }

    @DisplayName("scheduledFlush 호출 시 버퍼에 남은 로그를 HDFS에 쓴다")
    @Test
    void scheduledFlush_flushesRemainingBuffer() throws Exception {
        // given - 3개 메시지만 쌓기 (임계치 미만)
        for (int i = 0; i < 3; i++) {
            consumer.consume(createGoLogJson(1L, (long) (100 + i)));
        }
        verify(hdfsLogWriter, never()).writeLogs(any());

        // when - 스케줄러가 flush 호출
        consumer.scheduledFlush();

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UserGoLogMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(hdfsLogWriter, times(1)).writeLogs(captor.capture());
        assertThat(captor.getValue()).hasSize(3);
    }

    @DisplayName("버퍼가 비어있으면 scheduledFlush가 HDFS 쓰기를 호출하지 않는다")
    @Test
    void scheduledFlush_emptyBuffer_doesNothing() {
        // when
        consumer.scheduledFlush();

        // then
        verify(hdfsLogWriter, never()).writeLogs(any());
    }

    @DisplayName("HDFS 쓰기 실패 시 로그를 버퍼에 재추가한다")
    @Test
    void flush_hdfsFails_reBuffersLogs() throws Exception {
        // given - 10개 쌓아서 flush 트리거, HDFS 실패
        doThrow(new RuntimeException("HDFS down"))
                .doNothing()
                .when(hdfsLogWriter).writeLogs(any());

        for (int i = 0; i < 10; i++) {
            consumer.consume(createGoLogJson(1L, (long) (100 + i)));
        }

        // HDFS 실패 후 버퍼에 10개가 재추가됨
        // 추가로 1개 더 보내면 총 11개 -> 다시 flush
        // (재시도 시 성공)
        // 하지만 이건 구현 내부 동작이므로 scheduledFlush로 검증
        consumer.scheduledFlush();

        // then - 두 번째 호출은 성공해야 함
        verify(hdfsLogWriter, times(2)).writeLogs(any());
    }

    @DisplayName("잘못된 JSON 메시지는 버퍼에 추가하지 않는다")
    @Test
    void consume_invalidJson_doesNotBuffer() {
        // when
        consumer.consume("not a json");

        // then - flush 해봐도 아무것도 없음
        consumer.scheduledFlush();
        verify(hdfsLogWriter, never()).writeLogs(any());
    }

    // -- 헬퍼 --

    private String createGoLogJson(Long userId, Long placeId) throws Exception {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", userId);
        map.put("nationality", "KR");
        map.put("age", 30);
        map.put("gender", "female");
        map.put("travelPurpose", "leisure");
        map.put("lifestyle", "relaxed");
        map.put("action", "GO");
        map.put("placeId", placeId);
        map.put("currentLat", new BigDecimal("37.50"));
        map.put("currentLng", new BigDecimal("127.00"));
        map.put("reachLat", new BigDecimal("37.55"));
        map.put("reachLng", new BigDecimal("127.05"));
        map.put("timestamp", "2026-03-26T10:00:00");
        return objectMapper.writeValueAsString(map);
    }
}
