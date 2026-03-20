package com.e103.ohmyguide.domain.auth.oauth2;

import com.e103.ohmyguide.domain.auth.security.UserPrincipal;
import com.e103.ohmyguide.domain.auth.service.OAuth2UserProcessingService;
import com.e103.ohmyguide.domain.auth.user.OAuth2UserInfo;
import com.e103.ohmyguide.domain.auth.user.OAuth2UserInfoFactory;
import com.e103.ohmyguide.domain.user.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserProcessingService oauth2UserProcessingService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest, oAuth2User.getAttributes());
        AuthProvider provider = AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId());

        User user = oauth2UserProcessingService.processOAuth2User(provider, oAuth2UserInfo);

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }
}
