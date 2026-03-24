package com.e103.ohmyguide.domain.user.controller;


import com.e103.ohmyguide.domain.auth.security.CurrentUser;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.user.dto.OnboardingRequest;
import com.e103.ohmyguide.domain.user.dto.UserResponse;
import com.e103.ohmyguide.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public UserResponse getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userService.getCurrentUser(userPrincipal.getId());
    }

    @PutMapping("/user/onboarding")
    @PreAuthorize("hasRole('USER')")
    public UserResponse completeOnboarding(@CurrentUser UserPrincipal userPrincipal,
                                   @Valid @RequestBody OnboardingRequest request) {
        return userService.completeOnboarding(userPrincipal.getId(), request);
    }
}
