package com.e103.ohmyguide.domain.theme.controller;

import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.theme.service.ThemeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/themes")
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
}
