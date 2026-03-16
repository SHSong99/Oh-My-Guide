package com.e103.ohmyguide.domain.userphrase.entity;

import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "user_phrases",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "phrase_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserPhrase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_phrase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phrase_id", nullable = false)
    private Phrase phrase;

    @Builder
    private UserPhrase(User user, Phrase phrase) {
        this.user = user;
        this.phrase = phrase;
    }
}
