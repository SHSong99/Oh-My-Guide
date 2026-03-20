package com.e103.ohmyguide.domain.popularplace.service;

import com.e103.ohmyguide.domain.popularplace.dto.PopularPlaceResponse;
import com.e103.ohmyguide.domain.popularplace.repository.PopularPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        return popularPlaceRepository.findByCluster(nationality, ageGroup, gender, travelPurpose, lifestyle)
                .stream()                              // Entity 리스트를 스트림으로
                .map(PopularPlaceResponse::from)        // 각 Entity를 Response DTO로 변환
                .toList();                             // 다시 리스트로
    }
}