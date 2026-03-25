package com.e103.ohmyguide.domain.guide.service;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.guide.dto.*;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.uservisited.entity.UserVisited;
import com.e103.ohmyguide.domain.uservisited.repository.UserVisitedRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideService {

    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final UserVisitedRepository userVisitedRepository;

    private static final String TOPIC = "user-go-log";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public GuideGoResponse startNavigation(Long userId, Long placeId,
                                           BigDecimal currentLat, BigDecimal currentLng,
                                           BigDecimal reachLat, BigDecimal reachLng) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserGoLogMessage logRequest = UserGoLogMessage.toMessage(user, placeId, currentLat, currentLng, reachLat, reachLng);

        try {
            String message = objectMapper.writeValueAsString(logRequest);
            kafkaTemplate.send(TOPIC, message);
            log.debug("Sent user log to Kafka: action={}, placeId={}", logRequest.getAction(), logRequest.getPlaceId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user log request", e);
        }

        Attraction attraction = attractionRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", placeId));

        return GuideGoResponse.builder()
                .startLocation(StartLocationResponse.builder()
                        .latitude(currentLat)
                        .longitude(currentLng)
                        .build())
                .destination(GuideResponse.from(attraction))
                .build();
    }


    private void saveUserVisitedIfNotExists(User user, Attraction attraction) {
        if (!userVisitedRepository.existsByUserAndAttraction(user, attraction)) {
            UserVisited userVisited = UserVisited.builder()
                    .user(user)
                    .attraction(attraction)
                    .build();
            userVisitedRepository.save(userVisited);
        }
    }
}
