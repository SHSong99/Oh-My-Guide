package com.e103.ohmyguide.domain.themeattraction.entity;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.theme.entity.Theme;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "theme_attraction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThemeAttraction extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_attraction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attraction_id")
    private Attraction attraction;

    @Builder
    private ThemeAttraction(Theme theme, Attraction attraction) {
        this.theme = theme;
        this.attraction = attraction;
    }

    public void assignTheme(Theme theme) {
        this.theme = theme;
        theme.getThemeAttractions().add(this);
    }

    public void assignAttraction(Attraction attraction) {
        this.attraction = attraction;
        attraction.getThemeAttractions().add(this);
    }
}
