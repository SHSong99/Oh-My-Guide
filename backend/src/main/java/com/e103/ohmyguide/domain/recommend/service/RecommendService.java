package com.e103.ohmyguide.domain.recommend.service;

import com.e103.ohmyguide.domain.recommend.dto.AiRefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshResponse;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.uservisit.entity.UserVisit;
import com.e103.ohmyguide.domain.uservisit.repository.UserVisitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RestTemplate restTemplate;
    private final UserVisitRepository userVisitRepository;
    private final UserRepository userRepository;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public RefreshResponse getRecommendation(Long userId, String category, Double lat, Double lng) {
        List<Long> visitedIds = userVisitRepository.findAttrIdsByUserId(userId);
        String excludedParam = visitedIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(aiServerUrl + "/userRecommend")
                .queryParam("userId", userId)
                .queryParam("currentLat", lat)
                .queryParam("currentLng", lng)
                .queryParam("category", category != null ? category : "")
                .queryParam("excludedAttrIds", excludedParam);

        // 사용자 프로필 전달 (리랭킹용)
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            if (user.getAge() != null) builder.queryParam("age", user.getAge());
            if (user.getGender() != null) builder.queryParam("gender", user.getGender());
            if (user.getTravelPurpose() != null) builder.queryParam("companion", user.getTravelPurpose());
            if (user.getNationality() != null) builder.queryParam("country", user.getNationality());
        }

        log.info("RecommendService: ai 서버로 요청 전송");
        ResponseEntity<RefreshResponse> response = restTemplate.getForEntity(builder.toUriString(), RefreshResponse.class);
        log.info("RecommendService: ai 서버로 요청 종료");
        return response.getBody();
    }

    public RefreshResponse refreshRecommendation(Long userId, RefreshRequest request) {
        List<Long> visitedIds = userVisitRepository.findAttrIdsByUserId(userId);
        List<Integer> excludedIds = visitedIds.stream().map(Long::intValue).collect(Collectors.toList());
        if (request.getExcludedAttrIds() != null) {
            excludedIds.addAll(request.getExcludedAttrIds());
        }

        AiRefreshRequest aiRequest = AiRefreshRequest.builder()
                .userId(userId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radiusKm(request.getRadiusKm() != null ? request.getRadiusKm() : 10.0)
                .category(request.getCategory())
                .mood(request.getMood())
                .freeText(request.getFreeText())
                .excludedAttrIds(excludedIds)
                .build();

        ResponseEntity<RefreshResponse> response = restTemplate.postForEntity(
                aiServerUrl + "/userRecommend/recommend/refresh",
                aiRequest,
                RefreshResponse.class
        );

        return response.getBody();
    }

    public void visitPlace(Long userId, Long attrId) {
        if (!userVisitRepository.existsByUserIdAndAttrId(userId, attrId)) {
            userVisitRepository.save(UserVisit.builder().userId(userId).attrId(attrId).build());
        }
    }
}
