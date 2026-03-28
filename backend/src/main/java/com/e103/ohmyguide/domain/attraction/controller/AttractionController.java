package com.e103.ohmyguide.domain.attraction.controller;

import com.e103.ohmyguide.domain.attraction.controller.request.AttractionCreateRequest;
import com.e103.ohmyguide.domain.attraction.controller.request.AttractionUpdateRequest;
import com.e103.ohmyguide.domain.attraction.controller.response.GuideMessageResponse;
import com.e103.ohmyguide.domain.attraction.dto.AttractionDetailResponse;
import com.e103.ohmyguide.domain.attraction.service.AttractionService;
import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @GetMapping("/{attrId}")
    public AttractionDetailResponse getAttraction(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long attrId) {
        return attractionService.getAttractionDetail(userPrincipal.getId(), attrId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{attractionId}/guideMessage")
    public ResponseEntity<GuideMessageResponse> getGuideMessage(@PathVariable("attractionId") Long attractionId) {
        String guideMessage = attractionService.getGuideMessageBy(attractionId);
        return ResponseEntity.ok(GuideMessageResponse.from(guideMessage));
    }

    @PostMapping
    public ResponseEntity<AttractionDetailResponse> createAttraction(
            @Valid @RequestBody AttractionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attractionService.createAttraction(request.toServiceRequest()));
    }

    @PatchMapping("/{attractionId}")
    public ResponseEntity<AttractionDetailResponse> updateAttraction(
            @PathVariable Long attractionId,
            @RequestBody AttractionUpdateRequest request) {
        return ResponseEntity.ok(attractionService.updateAttraction(attractionId, request.toServiceRequest()));
    }
}
