package com.e103.ohmyguide.domain.auth.controller;

import com.e103.ohmyguide.domain.auth.dto.AuthResponse;
import com.e103.ohmyguide.domain.auth.dto.GoogleLoginRequest;
import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.auth.security.TokenProvider;
import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.auth.service.OAuth2UserProcessingService;
import com.e103.ohmyguide.domain.auth.user.GoogleOAuth2UserInfo;
import com.e103.ohmyguide.domain.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

    private final OAuth2UserProcessingService userProcessingService;
    private final TokenProvider tokenProvider;
    private final RestTemplate restTemplate;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        Map<String, Object> userAttributes;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(request.accessToken());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    GOOGLE_USERINFO_URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            userAttributes = response.getBody();
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (userAttributes == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        GoogleOAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(userAttributes);
        User user = userProcessingService.processOAuth2User(AuthProvider.google, userInfo);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, userPrincipal.getAuthorities()
        );

        String token = tokenProvider.createToken(authentication);
        return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
    }
}
