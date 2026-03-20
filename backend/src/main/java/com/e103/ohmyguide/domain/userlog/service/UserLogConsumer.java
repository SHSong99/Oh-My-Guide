package com.e103.ohmyguide.domain.userlog.service;

import com.e103.ohmyguide.domain.userlog.dto.UserLogRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogConsumer {

    private static final int FLUSH_THRESHOLD = 100;

    private final HdfsLogWriter hdfsLogWriter;
    private final ObjectMapper objectMapper;

    private final List<UserLogRequest> buffer = new ArrayList<>();

    @KafkaListener(topics = "user-log", groupId = "${spring.kafka.consumer.group-id}")
    public synchronized void consume(String message) {
        try {
            UserLogRequest logRequest = objectMapper.readValue(message, UserLogRequest.class);
            buffer.add(logRequest);
            log.debug("Buffered user log, buffer size: {}", buffer.size());

            if (buffer.size() >= FLUSH_THRESHOLD) {
                flush();
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user log message: {}", message, e);
        }
    }

    @Scheduled(fixedRate = 60000)
    public synchronized void scheduledFlush() {
        if (!buffer.isEmpty()) {
            flush();
        }
    }

    private void flush() {
        List<UserLogRequest> logsToWrite = new ArrayList<>(buffer);
        buffer.clear();

        try {
            hdfsLogWriter.writeLogs(logsToWrite);
        } catch (Exception e) {
            log.error("Failed to flush logs to HDFS, re-buffering {} logs", logsToWrite.size(), e);
            buffer.addAll(logsToWrite);
        }
    }
}