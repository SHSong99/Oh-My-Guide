package com.e103.ohmyguide.domain.auth.service;

import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.auth.user.OAuth2UserInfo;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.global.exception.OAuth2AuthenticationProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuth2UserProcessingService {

    private final UserRepository userRepository;

    public User processOAuth2User(AuthProvider provider, OAuth2UserInfo userInfo) {
        if (!StringUtils.hasText(userInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (!existingUser.getProvider().equals(provider)) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        existingUser.getProvider() + " account. Please use your " + existingUser.getProvider() +
                        " account to login.");
            }
            existingUser.updateOAuth2UserInfo(userInfo.getName(), userInfo.getImageUrl());
            return userRepository.save(existingUser);
        }

        User newUser = User.oauth2Builder()
                .provider(provider)
                .providerId(userInfo.getId())
                .name(userInfo.getName())
                .email(userInfo.getEmail())
                .imageUrl(userInfo.getImageUrl())
                .build();

        return userRepository.save(newUser);
    }
}
