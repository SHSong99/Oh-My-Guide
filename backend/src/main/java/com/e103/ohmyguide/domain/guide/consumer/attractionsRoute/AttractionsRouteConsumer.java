package com.e103.ohmyguide.domain.guide.consumer.attractionsRoute;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.guide.dto.AttractionsRouteResponseMessage;
import com.e103.ohmyguide.domain.guide.dto.GuideNavigationResponse;
import com.e103.ohmyguide.domain.guide.dto.GuideResponse;
import com.e103.ohmyguide.domain.guide.dto.StartLocationResponse;
import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Profile("attractions-route-consumer")
@Slf4j
@Service
@RequiredArgsConstructor
public class AttractionsRouteConsumer {

    private static final String RESPONSE_TOPIC = "attractions-route-response";

    private final AttractionRepository attractionRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(topics = "user-go-log", groupId = "attractions-route-group")
    public void consume(String message) {
        log.info("AttractionsRouteConsumer.consume: consumer 동작!!!! message = {}", message);

        UserGoLogMessage logMessage;
        try {
            logMessage = objectMapper.readValue(message, UserGoLogMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user-go-log message", e);
            return;
        }

        Long placeId = logMessage.getPlaceId();
        BigDecimal currentLat = logMessage.getCurrentLat();
        BigDecimal currentLng = logMessage.getCurrentLng();
        BigDecimal reachLat = logMessage.getReachLat();
        BigDecimal reachLng = logMessage.getReachLng();

        Attraction attraction = attractionRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", placeId));

        // 출발지 정보
        StartLocationResponse startLocation = StartLocationResponse.builder()
                .latitude(currentLat)
                .longitude(currentLng)
                .build();

        // 목적지 정보
        GuideResponse destination = GuideResponse.from(attraction);

        // 출발지 ↔ 목적지 바운딩 박스 내 장소들
        BigDecimal minLat = currentLat.min(reachLat);
        BigDecimal maxLat = currentLat.max(reachLat);
        BigDecimal minLng = currentLng.min(reachLng);
        BigDecimal maxLng = currentLng.max(reachLng);

        List<GuideResponse> nearbyPlaces = attractionRepository
                .findWithinBoundingBox(minLat, maxLat, minLng, maxLng, placeId)
                .stream()
                .map(GuideResponse::from)
                .toList();

        GuideNavigationResponse response = GuideNavigationResponse.builder()
                .startLocation(startLocation)
                .destination(destination)
                .nearbyPlaces(nearbyPlaces)
                .build();

        // 응답을 Kafka reply topic으로 발행 → 메인 컨테이너에서 SSE로 프론트에 전달
        AttractionsRouteResponseMessage responseMessage = AttractionsRouteResponseMessage.builder()
                .userId(logMessage.getUserId())
                .navigationResponse(response)
                .build();

        try {
            String responseJson = objectMapper.writeValueAsString(responseMessage);
            kafkaTemplate.send(RESPONSE_TOPIC, responseJson);
            log.info("Published navigation response to {}: userId={}, placeId={}, nearbyPlaces={}",
                    RESPONSE_TOPIC, logMessage.getUserId(), placeId, nearbyPlaces.size());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize navigation response: userId={}, placeId={}",
                    logMessage.getUserId(), placeId, e);
        }
    }
}
