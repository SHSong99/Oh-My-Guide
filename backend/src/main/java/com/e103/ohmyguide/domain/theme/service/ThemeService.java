package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.domain.theme.service.request.ThemeAttractionAddServiceRequest;
import com.e103.ohmyguide.domain.theme.service.request.ThemeCreateServiceRequest;
import com.e103.ohmyguide.domain.theme.service.request.ThemeUpdateServiceRequest;
import com.e103.ohmyguide.domain.theme.service.response.AttractionSummaryResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeDetailResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.service.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import com.e103.ohmyguide.domain.themeattraction.repository.ThemeAttractionRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final AttractionRepository attractionRepository;
    private final ThemeAttractionRepository themeAttractionRepository;

    @Transactional
    public void createTheme(ThemeCreateServiceRequest request) {
        Theme theme = Theme.builder()
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .region(request.getRegion())
                .build();
        themeRepository.save(theme);
    }

    @Transactional
    public void updateTheme(Long themeId, ThemeUpdateServiceRequest request) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", "themeId", themeId));
        theme.update(request.getName(), request.getDescription(), request.getCategory(), request.getRegion());
    }

    @Transactional
    public void deleteTheme(Long themeId) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", "themeId", themeId));
        themeRepository.delete(theme);
    }

    @Transactional
    public void addAttraction(Long themeId, ThemeAttractionAddServiceRequest request) {
        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", "themeId", themeId));
        Attraction attraction = attractionRepository.findById(request.getAttractionId())
                .orElseThrow(() -> new ResourceNotFoundException("Attraction", "attractionId", request.getAttractionId()));

        ThemeAttraction themeAttraction = ThemeAttraction.builder()
                .attractionOrder(request.getAttractionOrder())
                .build();
        themeAttraction.assignTheme(theme);
        themeAttraction.assignAttraction(attraction);
        themeAttractionRepository.save(themeAttraction);
    }

    @Transactional
    public void removeAttraction(Long themeId, Long attractionId) {
        themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException("Theme", "themeId", themeId));
        ThemeAttraction themeAttraction = themeAttractionRepository.findByTheme_IdAndAttraction_Id(themeId, attractionId)
                .orElseThrow(() -> new ResourceNotFoundException("ThemeAttraction", "attractionId", attractionId));
        themeAttractionRepository.delete(themeAttraction);
    }

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
                .sorted(Comparator.comparingInt(ThemeAttraction::getAttractionOrder))
                .map(AttractionSummaryResponse::from)
                .toList();

        return ThemeDetailResponse.of(theme, attractions);
    }
}
