package com.e103.ohmyguide.domain.themeattraction.repository;

import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThemeAttractionRepository extends JpaRepository<ThemeAttraction, Long> {

    Optional<ThemeAttraction> findByTheme_IdAndAttraction_Id(Long themeId, Long attractionId);
}
