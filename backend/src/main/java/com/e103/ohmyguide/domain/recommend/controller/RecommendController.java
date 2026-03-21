package com.e103.ohmyguide.domain.recommend.controller;

import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.recommend.dto.RefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshResponse;
import com.e103.ohmyguide.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/userRecommend")
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public ResponseEntity<RefreshResponse> getRecommendation(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) String category,
            @RequestParam Double currentLat,
            @RequestParam Double currentLng
    ) {
        RefreshResponse response = recommendService.getRecommendation(
                userPrincipal.getId(), category, currentLat, currentLng
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/recommend/refresh")
    public ResponseEntity<RefreshResponse> refreshRecommendation(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody RefreshRequest request
    ) {
        RefreshResponse response = recommendService.refreshRecommendation(
                userPrincipal.getId(), request
        );
        return ResponseEntity.ok(response);
    }
}
