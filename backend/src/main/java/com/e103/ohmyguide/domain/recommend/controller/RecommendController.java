package com.e103.ohmyguide.domain.recommend.controller;

import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.recommend.dto.RefreshRequest;
import com.e103.ohmyguide.domain.recommend.dto.RefreshResponse;
import com.e103.ohmyguide.domain.recommend.dto.VisitRequest;
import com.e103.ohmyguide.domain.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/userRecommend")
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

        log.info("RecommendController.getRecommendation: 사용자 추천 요청 lat = {}, lot = {}", currentLat, currentLng);
        RefreshResponse response = recommendService.getRecommendation(
                userPrincipal.getId(), category, currentLat, currentLng
        );

        log.info("RecommendController.getRecommendation: size = {} ", response.getRecommendations().size());
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
        log.info("RecommendController.refreshRecommendation: size = {} ", response.getRecommendations().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/visit")
    public ResponseEntity<Void> visitPlace(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestBody VisitRequest request
    ) {
        recommendService.visitPlace(userPrincipal.getId(), request.getAttrId());
        return ResponseEntity.ok().build();
    }
}
