package com.e103.ohmyguide.domain.guide.consumer.userVisited;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attractionvector.entity.AttractionVector;
import com.e103.ohmyguide.domain.attractionvector.repository.AttractionVectorRepository;
import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.uservector.entity.UserVector;
import com.e103.ohmyguide.domain.uservector.repository.UserVectorRepository;
import com.e103.ohmyguide.domain.uservisited.entity.UserVisited;
import com.e103.ohmyguide.domain.uservisited.repository.UserVisitedRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile("user-visited-consumer")
@Slf4j
@Service
@RequiredArgsConstructor
public class UserVisitedConsumer {

    private static final double LEARNING_RATE = 0.15;
    private static final TypeReference<List<Double>> VECTOR_TYPE = new TypeReference<>() {};

    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final UserVisitedRepository userVisitedRepository;
    private final UserVectorRepository userVectorRepository;
    private final AttractionVectorRepository attractionVectorRepository;
    private final ObjectMapper objectMapper;

    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(topics = "user-go-log", groupId = "user-visited-group")
    @Transactional
    public void consume(String message) {
        UserGoLogMessage logMessage;
        try {
            logMessage = objectMapper.readValue(message, UserGoLogMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user-go-log message", e);
            return;
        }

        Long userId = logMessage.getUserId();
        Long placeId = logMessage.getPlaceId();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found: userId={}", userId);
            return;
        }

        Attraction attraction = attractionRepository.findById(placeId).orElse(null);
        if (attraction == null) {
            log.warn("Attraction not found: placeId={}", placeId);
            return;
        }

        if (!userVisitedRepository.existsByUserAndAttraction(user, attraction)) {
            UserVisited userVisited = UserVisited.builder()
                    .user(user)
                    .attraction(attraction)
                    .build();
            userVisitedRepository.save(userVisited);
            log.info("Saved user visited: userId={}, placeId={}", userId, placeId);
        }

        updateUserVector(userId, placeId);
    }

    @RetryableTopic(
        attempts = "5",
        backoff = @Backoff(delay = 1000, multiplier = 2),
        dltTopicSuffix = ".dlt"
    )
    @KafkaListener(topics = "user-star-log", groupId = "user-visited-group")
    @Transactional
    public void consumeStarLog(String message) {
        UserGoLogMessage logMessage;
        try {
            logMessage = objectMapper.readValue(message, UserGoLogMessage.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize user-star-log message", e);
            return;
        }

        Integer star = logMessage.getStar();
        if (star == null || star > 3) {
            return;
        }

        // star 1, 2, 3 → reverse the vector update done on GO
        Long userId = logMessage.getUserId();
        Long placeId = logMessage.getPlaceId();
        revertUserVector(userId, placeId);
    }

    private void revertUserVector(Long userId, Long placeId) {
        UserVector userVector = userVectorRepository.findById(userId).orElse(null);
        AttractionVector attractionVector = attractionVectorRepository.findById(placeId).orElse(null);

        if (userVector == null || attractionVector == null) {
            log.warn("Vector not found for revert: userId={}, placeId={}", userId, placeId);
            return;
        }

        try {
            List<Double> uVec = objectMapper.readValue(userVector.getPreferenceVector(), VECTOR_TYPE);
            List<Double> aVec = objectMapper.readValue(attractionVector.getAttractionVector(), VECTOR_TYPE);

            if (uVec.size() != aVec.size()) {
                log.error("Vector dimension mismatch for revert: userVector={}, attractionVector={}", uVec.size(), aVec.size());
                return;
            }

            // Reverse: newUserVector = userVector - 0.15 * (attractionVector - userVector)
            List<Double> reverted = new java.util.ArrayList<>(uVec.size());
            for (int i = 0; i < uVec.size(); i++) {
                reverted.add(uVec.get(i) - LEARNING_RATE * (aVec.get(i) - uVec.get(i)));
            }

            userVector.updatePreferenceVector(objectMapper.writeValueAsString(reverted));
            log.info("Reverted user vector: userId={}, placeId={}", userId, placeId);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse vectors for revert: userId={}, placeId={}", userId, placeId, e);
        }
    }

    private void updateUserVector(Long userId, Long placeId) {
        UserVector userVector = userVectorRepository.findById(userId).orElse(null);
        AttractionVector attractionVector = attractionVectorRepository.findById(placeId).orElse(null);

        if (userVector == null || attractionVector == null) {
            log.warn("Vector not found: userId={}, placeId={}, userVector={}, attractionVector={}",
                    userId, placeId, userVector != null, attractionVector != null);
            return;
        }

        try {
            List<Double> uVec = objectMapper.readValue(userVector.getPreferenceVector(), VECTOR_TYPE);
            List<Double> aVec = objectMapper.readValue(attractionVector.getAttractionVector(), VECTOR_TYPE);

            if (uVec.size() != aVec.size()) {
                log.error("Vector dimension mismatch: userVector={}, attractionVector={}", uVec.size(), aVec.size());
                return;
            }

            // newUserVector = userVector + 0.15 * (attractionVector - userVector)
            List<Double> updated = new java.util.ArrayList<>(uVec.size());
            for (int i = 0; i < uVec.size(); i++) {
                updated.add(uVec.get(i) + LEARNING_RATE * (aVec.get(i) - uVec.get(i)));
            }

            userVector.updatePreferenceVector(objectMapper.writeValueAsString(updated));
            log.info("Updated user vector: userId={}, placeId={}", userId, placeId);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse vectors: userId={}, placeId={}", userId, placeId, e);
        }
    }
}
