package com.e103.ohmyguide.domain.user.entity;

import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.global.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    @Column(name = "nickname", length = 20)
    private String nickname;

    @JsonIgnore
    private String password;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "onboarding_completed", nullable = false)
    private Boolean onboardingCompleted = false;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    private String providerId;

    @Builder(builderClassName = "DefaultUserBuilder")
    private User(String email, String nickname, String profileImageUrl, Boolean onboardingCompleted) {
        this.email = email;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.onboardingCompleted = onboardingCompleted != null ? onboardingCompleted : false;
    }

    @Builder(builderMethodName = "oauth2Builder", builderClassName = "OAuth2UserBuilder")
    private User(String email, String name, String imageUrl, AuthProvider provider, String providerId) {
        this.email = email;
        this.name = name;
        this.imageUrl = imageUrl;
        this.provider = provider;
        this.providerId = providerId;
        this.onboardingCompleted = false;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateOAuth2UserInfo(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
