package com.e103.ohmyguide.domain.attraction.service;

import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionCreateServiceRequest;
import com.e103.ohmyguide.domain.attraction.service.request.AttractionUpdateServiceRequest;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;

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
                request.getFirstImage1(), request.getOverview());
        return AttractionDetailResponse.from(attraction);
    }
}
