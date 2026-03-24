package com.e103.ohmyguide.domain.userlog.service;

import com.e103.ohmyguide.domain.userlog.dto.UserLogRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLogProducer {

    private static final String TOPIC = "user-log";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendLog(UserLogRequest logRequest) {
        try {
            String message = objectMapper.writeValueAsString(logRequest);
            kafkaTemplate.send(TOPIC, message);
            log.debug("Sent user log to Kafka: action={}, placeId={}", logRequest.getAction(), logRequest.getPlaceId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user log request", e);
        }
    }
}
