package com.e103.ohmyguide.domain.guide.controller;

import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.guide.dto.GuideGoResponse;
import com.e103.ohmyguide.domain.guide.service.GuideService;
import com.e103.ohmyguide.domain.guide.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/guide")
@RequiredArgsConstructor
public class GuideController {

    private final GuideService guideService;
    private final SseEmitterManager sseEmitterManager;

    @GetMapping("/{placeId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GuideGoResponse> startNavigation(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable Long placeId,
            @RequestParam BigDecimal currentLat,
            @RequestParam BigDecimal currentLng,
            @RequestParam BigDecimal reachLat,
            @RequestParam BigDecimal reachLng
    ) {
        Long userId = userPrincipal.getId();
        log.info("GuideController.startNavigation: userId = {} 호출!!", userId);

        GuideGoResponse response = guideService.startNavigation(userId, placeId,
                currentLat, currentLng, reachLat, reachLng);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/star")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> rateStar(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam @NotNull Long attrId,
            @RequestParam @Min(1) @Max(5) int star
    ) {
        guideService.rateStar(userPrincipal.getId(), attrId, star);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasRole('USER')")
    public SseEmitter connectSse(@CurrentUser UserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();

        log.info("GuideController.connectSse: sse 연결 요청!! user id = {}", userId);
        SseEmitter emitter = sseEmitterManager.create(userId);

        // Spring이 async 처리를 설정한 후 초기 이벤트 전송 (HTTP 응답 commit용)
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100);
                sseEmitterManager.sendConnected(userId);
            } catch (Exception e) {
                log.warn("Failed to send initial SSE event: userId={}", userId, e);
            }
        });

        log.info("GuideController.connectSse: sse 반환!! userId = {} emitter = {}", userId, emitter);
        return emitter;
    }
}
