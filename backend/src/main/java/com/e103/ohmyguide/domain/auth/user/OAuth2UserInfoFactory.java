package com.e103.ohmyguide.domain.auth.user;


import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.global.exception.OAuth2AuthenticationProcessingException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(OAuth2UserRequest oAuth2UserRequest, Map<String, Object> attributes) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        if(registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
