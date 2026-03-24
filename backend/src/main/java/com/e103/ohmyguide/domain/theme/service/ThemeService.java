package com.e103.ohmyguide.domain.theme.service;

import com.e103.ohmyguide.domain.theme.response.ThemeInfoResponse;
import com.e103.ohmyguide.domain.theme.response.ThemeInfosResponse;
import com.e103.ohmyguide.domain.theme.repository.ThemeRepository;
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
}
