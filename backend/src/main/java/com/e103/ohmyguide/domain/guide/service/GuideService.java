package com.e103.ohmyguide.domain.guide.service;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.guide.dto.GuideNavigationResponse;
import com.e103.ohmyguide.domain.guide.dto.GuideResponse;
import com.e103.ohmyguide.domain.guide.dto.StartLocationResponse;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.userlog.dto.UserLogRequest;
import com.e103.ohmyguide.domain.userlog.service.UserLogProducer;
import com.e103.ohmyguide.domain.uservisited.entity.UserVisited;
import com.e103.ohmyguide.domain.uservisited.repository.UserVisitedRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuideService {

    private final UserRepository userRepository;
    private final AttractionRepository attractionRepository;
    private final UserVisitedRepository userVisitedRepository;
    private final UserLogProducer userLogProducer;

    @Transactional
    public GuideNavigationResponse startNavigation(Long userId, Long placeId,
                                                    BigDecimal currentLat, BigDecimal currentLng,
                                                    BigDecimal reachLat, BigDecimal reachLng) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Attraction attraction = attractionRepository.findById(placeId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", placeId));

        sendGoLog(user, placeId);
        saveUserVisitedIfNotExists(user, attraction);

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

        return GuideNavigationResponse.builder()
                .startLocation(startLocation)
                .destination(destination)
                .nearbyPlaces(nearbyPlaces)
                .build();
    }

    private void sendGoLog(User user, Long placeId) {
        UserLogRequest logRequest = UserLogRequest.builder()
                .userId(user.getId())
                .nationality(user.getNationality())
                .age(user.getAge() != null ? user.getAge() : 0)
                .gender(user.getGender() != null ? user.getGender() : "unknown")
                .travelPurpose(user.getTravelPurpose() != null ? user.getTravelPurpose() : "general")
                .lifestyle(user.getLifestyle() != null ? user.getLifestyle() : "standard")
                .action("GO")
                .placeId(placeId)
                .timestamp(LocalDateTime.now().toString())
                .build();

        userLogProducer.sendLog(logRequest);
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
