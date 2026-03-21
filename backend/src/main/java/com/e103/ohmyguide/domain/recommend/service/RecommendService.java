package com.e103.ohmyguide.domain.recommend.service;

import com.e103.ohmyguide.domain.recommend.dto.AiRefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    public RefreshResponse getRecommendation(Long userId, String category, Double lat, Double lng) {
        String url = UriComponentsBuilder.fromHttpUrl(aiServerUrl + "/api/userRecommend")
                .queryParam("userId", userId)
                .queryParam("currentLat", lat)
                .queryParam("currentLng", lng)
                .queryParam("category", category != null ? category : "")
                .toUriString();

        ResponseEntity<RefreshResponse> response = restTemplate.getForEntity(url, RefreshResponse.class);
        return response.getBody();
    }

    public RefreshResponse refreshRecommendation(Long userId, RefreshRequest request) {
        AiRefreshRequest aiRequest = AiRefreshRequest.builder()
                .userId(userId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radiusKm(request.getRadiusKm() != null ? request.getRadiusKm() : 10.0)
                .category(request.getCategory())
                .mood(request.getMood())
                .freeText(request.getFreeText())
                .excludedAttrIds(request.getExcludedAttrIds() != null ? request.getExcludedAttrIds() : Collections.emptyList())
                .build();

        ResponseEntity<RefreshResponse> response = restTemplate.postForEntity(
                aiServerUrl + "/api/userRecommend/recommend/refresh",
                aiRequest,
                RefreshResponse.class
        );

        return response.getBody();
    }
}
