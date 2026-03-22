package com.e103.ohmyguide.domain.attraction.service;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
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
}
