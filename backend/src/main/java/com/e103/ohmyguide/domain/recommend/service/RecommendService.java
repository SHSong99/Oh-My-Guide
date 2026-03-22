package com.e103.ohmyguide.domain.recommend.service;

import com.e103.ohmyguide.domain.recommend.dto.AiRefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshResponse;
import com.e103.ohmyguide.domain.uservisit.entity.UserVisit;
import com.e103.ohmyguide.domain.uservisit.repository.UserVisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RestTemplate restTemplate;
    private final UserVisitRepository userVisitRepository;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public RefreshResponse getRecommendation(Long userId, String category, Double lat, Double lng) {
        List<Long> visitedIds = userVisitRepository.findAttrIdsByUserId(userId);
        String excludedParam = visitedIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String url = UriComponentsBuilder.fromHttpUrl(aiServerUrl + "/api/userRecommend")
                .queryParam("userId", userId)
                .queryParam("currentLat", lat)
                .queryParam("currentLng", lng)
                .queryParam("category", category != null ? category : "")
                .queryParam("excludedAttrIds", excludedParam)
                .toUriString();

        ResponseEntity<RefreshResponse> response = restTemplate.getForEntity(url, RefreshResponse.class);
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
                aiServerUrl + "/api/userRecommend/recommend/refresh",
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
