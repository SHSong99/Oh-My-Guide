package com.e103.ohmyguide.domain.attraction.controller;

import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attraction")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionRepository attractionRepository;

    @GetMapping("/{attrId}")
    public AttractionDetailResponse getAttraction(@PathVariable Long attrId) {
        Attraction attraction = attractionRepository.findById(attrId)
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "id", attrId));
        return AttractionDetailResponse.from(attraction);
    }
}
