package com.e103.ohmyguide.domain.guide.controller;

import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.guide.dto.GuideNavigationResponse;
import com.e103.ohmyguide.domain.guide.service.GuideService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/guide")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;

    @GetMapping("/{placeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GuideNavigationResponse> startNavigation(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long placeId,
            @RequestParam BigDecimal currentLat,
            @RequestParam BigDecimal currentLng,
            @RequestParam BigDecimal reachLat,
            @RequestParam BigDecimal reachLng,
            @RequestParam(required = false) String trafic
    ) {
        GuideNavigationResponse response = guideService.startNavigation(
                userPrincipal.getId(), placeId,
                currentLat, currentLng, reachLat, reachLng
        );
        return ResponseEntity.ok(response);
    }
}
