package com.e103.ohmyguide.domain.guide.consumer.attractionsRoute;

import com.e103.ohmyguide.domain.guide.dto.AttractionsRouteResponseMessage;
import com.e103.ohmyguide.domain.guide.service.SseEmitterManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Profile("default")
@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionsRouteResponseConsumer {

    private final ObjectMapper objectMapper;
    private final SseEmitterManager sseEmitterManager;

    @KafkaListener(topics = "attractions-route-response", groupId = "attractions-route-response-group")
    public void consume(String message) {
        AttractionsRouteResponseMessage responseMessage;
        try {
            responseMessage = objectMapper.readValue(message, AttractionsRouteResponseMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize attractions-route-response message", e);
            return;
        }

        sseEmitterManager.send(responseMessage.getUserId(), responseMessage.getNavigationResponse());
        log.info("Sent navigation response via SSE: userId={}", responseMessage.getUserId());
    }
}
