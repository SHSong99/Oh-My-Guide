package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.domain.theme.service.response.AttractionSummaryResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;

    public ThemeInfosResponse getThemes() {
        List<ThemeInfoResponse> themes = themeRepository.findAll()
                .stream()
                .map(ThemeInfoResponse::from)
                .toList();
        return ThemeInfosResponse.of(themes);
    }

    public ThemeDetailResponse getTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", "themeId", themeId));

        List<AttractionSummaryResponse> attractions = theme.getThemeAttractions()
                .stream()
                .map(ta -> AttractionSummaryResponse.from(ta.getAttraction()))
                .toList();

        return ThemeDetailResponse.of(theme, attractions);
    }
}
