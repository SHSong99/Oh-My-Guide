package com.e103.ohmyguide.domain.attraction.service;

import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionCreateServiceRequest;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionUpdateServiceRequest;
import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttractionService {

    private static final String VIEW_LOG_TOPIC = "user-view-log";

    private final AttractionRepository attractionRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public AttractionDetailResponse getAttractionDetail(Long userId, Long attrId) {
        Attraction attraction = attractionRepository.findById(attrId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attrId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            String message = objectMapper.writeValueAsString(UserGoLogMessage.toViewMessage(user, attrId));
            kafkaTemplate.send(VIEW_LOG_TOPIC, message);
            log.debug("Sent view log to Kafka: userId={}, placeId={}", userId, attrId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize view log message", e);
        }

        return AttractionDetailResponse.from(attraction);
    }

    public String getGuideMessageBy(Long attractionId) {
        return attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attractionId))
                .getOverviewTts();
    }

    @Transactional
    public AttractionDetailResponse createAttraction(AttractionCreateServiceRequest request) {
        Attraction attraction = Attraction.builder()
                .title(request.getTitle())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .firstImage1(request.getFirstImage1())
                .overview(request.getOverview())
                .overviewTts(request.getOverviewTts())
                .build();
        return AttractionDetailResponse.from(attractionRepository.save(attraction));
    }

    @Transactional
    public AttractionDetailResponse updateAttraction(Long attractionId, AttractionUpdateServiceRequest request) {
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attractionId));
        attraction.update(request.getTitle(), request.getLatitude(), request.getLongitude(),
                request.getFirstImage1(), request.getOverview(), request.getOverviewTts());
        return AttractionDetailResponse.from(attraction);
    }
}
