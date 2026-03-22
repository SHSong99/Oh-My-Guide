package com.e103.ohmyguide.domain.attraction.controller;

import com.e103.ohmyguide.domain.attraction.controller.response.GuideMessageResponse;
import com.e103.ohmyguide.domain.attraction.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{attractionId}/guideMessage")
    public ResponseEntity<GuideMessageResponse> getGuideMessage(@PathVariable("attractionId") Long attractionId) {
        String guideMessage = attractionService.getGuideMessageBy(attractionId);
        return ResponseEntity.ok(GuideMessageResponse.from(guideMessage));
    }
}
