package com.e103.ohmyguide;

import com.e103.ohmyguide.domain.auth.security.TokenProvider;
import com.e103.ohmyguide.domain.auth.service.OAuth2UserProcessingService;
import com.e103.ohmyguide.domain.popularplace.controller.PopularPlaceController;
import com.e103.ohmyguide.domain.popularplace.service.PopularPlaceService;
import com.e103.ohmyguide.domain.popularplace.service.SparkJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(controllers = {
        PopularPlaceController.class
}, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class
})
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected PopularPlaceService popularPlaceService;

    @MockitoBean
    protected SparkJobService sparkJobService;

    @MockitoBean
    protected OAuth2UserProcessingService oAuth2UserProcessingService;

    @MockitoBean
    protected TokenProvider tokenProvider;

}
