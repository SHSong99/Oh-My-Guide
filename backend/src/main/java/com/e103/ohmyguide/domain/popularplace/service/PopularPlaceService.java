package com.e103.ohmyguide.domain.popularplace.service;

import com.e103.ohmyguide.domain.popularplace.dto.PopularPlaceResponse;
import com.e103.ohmyguide.domain.popularplace.repository.PopularPlaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)     // 이 서비스는 DB를 읽기만 함 → 성능 최적화
public class PopularPlaceService {

    private final PopularPlaceRepository popularPlaceRepository;

    public List<PopularPlaceResponse> getRecommendations(
            String nationality,
            int age,
            String gender,
            String travelPurpose
    ) {
        String ageGroup = toAgeGroup(age);
        List<PopularPlaceResponse> popularPlaces = popularPlaceRepository.findByCluster(nationality, ageGroup, gender, travelPurpose)
                .stream()                              // Entity 리스트를 스트림으로
                .map(PopularPlaceResponse::from)        // 각 Entity를 Response DTO로 변환
                .toList();// 다시 리스트로

        log.info("PopularPlaceService.getRecommendations: query by nationality = {} age group = {} gender = {} travelPurpose = {}",
                nationality, ageGroup, gender, travelPurpose);
        log.info("PopularPlaceService.getRecommendations: result size = {}",  popularPlaces.size());

        return popularPlaces;
    }

    // Spark analyze_logs.py 와 동일한 나이대 분류 기준
    private String toAgeGroup(int age) {
        if (age < 20) return "10s";
        if (age < 30) return "20s";
        if (age < 40) return "30s";
        if (age < 50) return "40s";
        if (age < 60) return "50s";
        if (age < 70) return "60s";
        return "70s+";
    }
}