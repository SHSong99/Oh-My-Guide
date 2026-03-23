package com.e103.ohmyguide.domain.user.dto;

import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String imageUrl;
    private String nickname;
    private String profileImageUrl;
    private Boolean onboardingCompleted;
    private AuthProvider provider;
    private String providerId;
    private String nationality;
    private Integer age;
    private String gender;
    private String travelPurpose;
    private String lifestyle;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .imageUrl(user.getImageUrl())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .onboardingCompleted(user.getOnboardingCompleted())
                .provider(user.getProvider())
                .providerId(user.getProviderId())
                .nationality(user.getNationality())
                .age(user.getAge())
                .gender(user.getGender())
                .travelPurpose(user.getTravelPurpose())
                .lifestyle(user.getLifestyle())
                .build();
    }
}
