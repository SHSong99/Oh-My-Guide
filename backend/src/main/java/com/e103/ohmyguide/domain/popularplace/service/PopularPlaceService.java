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
            String ageGroup,
            String gender,
            String travelPurpose,
            String lifestyle
    ) {
        List<PopularPlaceResponse> popularPlaces = popularPlaceRepository.findByCluster(nationality, ageGroup, gender, travelPurpose, lifestyle)
                .stream()                              // Entity 리스트를 스트림으로
                .map(PopularPlaceResponse::from)        // 각 Entity를 Response DTO로 변환
                .toList();// 다시 리스트로

        log.info("PopularPlaceService.getRecommendations: query by nationality = {} age group = {} gender = {} travelPurpose = {} lifestyle = {}",
                nationality, ageGroup, gender, travelPurpose, lifestyle);
        log.info("PopularPlaceService.getRecommendations: result size = {}",  popularPlaces.size());

        return popularPlaces;
    }
}