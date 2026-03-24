package com.e103.ohmyguide.domain.theme.controller;

import com.e103.ohmyguide.domain.theme.controller.request.ThemeAttractionAddRequest;
import com.e103.ohmyguide.domain.theme.controller.request.ThemeCreateRequest;
import com.e103.ohmyguide.domain.theme.controller.request.ThemeUpdateRequest;
import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.theme.service.ThemeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<ThemeInfosResponse> getThemes() {
        return ResponseEntity.ok(themeService.getThemes());
    }

    @GetMapping("/{themeId}")
    public ResponseEntity<ThemeDetailResponse> getTheme(@PathVariable Long themeId) {
        return ResponseEntity.ok(themeService.getTheme(themeId));
    }

    @PostMapping
    public ResponseEntity<Void> createTheme(@Valid @RequestBody ThemeCreateRequest request) {
        themeService.createTheme(request.toServiceRequest());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{themeId}")
    public ResponseEntity<Void> updateTheme(
            @PathVariable Long themeId,
            @Valid @RequestBody ThemeUpdateRequest request) {
        themeService.updateTheme(themeId, request.toServiceRequest());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{themeId}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long themeId) {
        themeService.deleteTheme(themeId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{themeId}/attractions")
    public ResponseEntity<Void> addAttraction(
            @PathVariable Long themeId,
            @Valid @RequestBody ThemeAttractionAddRequest request) {
        themeService.addAttraction(themeId, request.toServiceRequest());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{themeId}/attractions/{attractionId}")
    public ResponseEntity<Void> removeAttraction(
            @PathVariable Long themeId,
            @PathVariable Long attractionId) {
        themeService.removeAttraction(themeId, attractionId);
        return ResponseEntity.noContent().build();
    }
}
