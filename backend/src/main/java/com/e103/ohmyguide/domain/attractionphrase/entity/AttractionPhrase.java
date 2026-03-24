package com.e103.ohmyguide.domain.attractionphrase.entity;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "attraction_phrases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attr_id", "phrase_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttractionPhrase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attraction_phrase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attr_id", nullable = false)
    private Attraction attraction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id", nullable = false)
    private Phrase phrase;

    @Builder
    private AttractionPhrase(Attraction attraction, Phrase phrase) {
        this.attraction = attraction;
        this.phrase = phrase;
    }
}
