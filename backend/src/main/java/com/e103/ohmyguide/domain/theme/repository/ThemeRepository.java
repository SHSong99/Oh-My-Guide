package com.e103.ohmyguide.domain.theme.repository;

import com.e103.ohmyguide.domain.theme.entity.Theme;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
