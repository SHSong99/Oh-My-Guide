package com.e103.ohmyguide.domain.phrase.entity;

import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "phrases")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Phrase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phrase_id")
    private Long id;

    @Column(name = "content", length = 100, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private PhraseLanguage language;

    @Builder
    private Phrase(String content, PhraseLanguage language) {
        this.content = content;
        this.language = language != null ? language : PhraseLanguage.KOR;
    }
}
