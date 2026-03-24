package com.e103.ohmyguide.domain.user.service;

import com.e103.ohmyguide.domain.user.dto.OnboardingRequest;
import com.e103.ohmyguide.domain.user.dto.UserResponse;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse completeOnboarding(Long userId, OnboardingRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.completeOnboarding(request.getNationality(), request.getAge(), request.getGender(), request.getCompanion(), request.getCountry());
        return UserResponse.from(user);
    }
}
