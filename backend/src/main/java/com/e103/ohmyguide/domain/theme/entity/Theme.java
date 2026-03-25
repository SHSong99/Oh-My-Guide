package com.e103.ohmyguide.domain.theme.entity;

import com.e103.ohmyguide.domain.themeattraction.entity.ThemeAttraction;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "themes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Theme extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "theme_id")
    private Long id;

    @Column(unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String region;

    @OneToMany(mappedBy = "theme")
    private List<ThemeAttraction> themeAttractions = new ArrayList<>();

    @Builder
    private Theme(String name, String description, String category, String region) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
    }

    public void update(String name, String description, String category, String region) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.region = region;
    }
}
