package com.e103.ohmyguide.domain.guide.consumer.HdfsLog;

import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Profile("hdfs-log-consumer")
@Slf4j
@Service
@RequiredArgsConstructor
public class HdfsLogConsumer {

    private static final int FLUSH_THRESHOLD = 10;

    private final HdfsLogWriter hdfsLogWriter;
    private final ObjectMapper objectMapper;

    private final List<UserGoLogMessage> buffer = new ArrayList<>();

    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(topics = {"user-go-log", "user-view-log", "user-star-log"}, groupId = "hdfs-log-group")
    public synchronized void consume(String message) {
        try {
            UserGoLogMessage logRequest = objectMapper.readValue(message, UserGoLogMessage.class);
            buffer.add(logRequest);
            log.debug("Buffered user log, buffer size: {}", buffer.size());

            if (buffer.size() >= FLUSH_THRESHOLD) {
                flush();
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user log message: {}", message, e);
        }
    }

    @Scheduled(fixedRate = 30000)
    public synchronized void scheduledFlush() {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        List<UserGoLogMessage> logsToWrite = new ArrayList<>(buffer);
        buffer.clear();

        try {
            hdfsLogWriter.writeLogs(logsToWrite);
        } catch (Exception e) {
            log.error("Failed to flush logs to HDFS, re-buffering {} logs", logsToWrite.size(), e);
            buffer.addAll(logsToWrite);
        }
    }
}
