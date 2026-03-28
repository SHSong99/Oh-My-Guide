package com.e103.ohmyguide.domain.guide.service;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.guide.dto.GuideGoResponse;
import com.e103.ohmyguide.domain.guide.dto.UserGoLogMessage;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {

    @InjectMocks
    private GuideService guideService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    // -- 테스트 픽스처 --

    private User createTestUser() {
        return User.oauth2Builder()
                .email("test@gmail.com")
                .name("TestUser")
                .provider(AuthProvider.google)
                .providerId("google-123")
                .build();
    }

    private Attraction createTestAttraction(BigDecimal lat, BigDecimal lng) {
        return Attraction.builder()
                .title("해운대 해수욕장")
                .addr1("부산광역시 해운대구")
                .latitude(lat)
                .longitude(lng)
                .firstImage1("https://example.com/image.jpg")
                .overview("해운대 해수욕장은 부산을 대표하는 해수욕장이다.")
                .overviewTts("해운대 해수욕장은 부산을 대표하는 해수욕장입니다.")
                .build();
    }

    // -- 테스트 --

    @DisplayName("GO 시 프론트에 출발지/목적지 정보를 즉시 반환한다")
    @Test
    void startNavigation_returnsGuideGoResponse() {
        // given
        Long userId = 1L;
        Long placeId = 100L;
        BigDecimal currentLat = new BigDecimal("37.5665");
        BigDecimal currentLng = new BigDecimal("126.9780");
        BigDecimal reachLat = new BigDecimal("35.1587");
        BigDecimal reachLng = new BigDecimal("129.1604");

        User user = createTestUser();
        user.completeOnboarding("US", 25, "male", "sightseeing", "active");

        Attraction attraction = createTestAttraction(reachLat, reachLng);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(attractionRepository.findById(placeId)).willReturn(Optional.of(attraction));

        // when
        GuideGoResponse response = guideService.startNavigation(
                userId, placeId, currentLat, currentLng, reachLat, reachLng);

        // then
        assertThat(response.getStartLocation().getLatitude()).isEqualByComparingTo(currentLat);
        assertThat(response.getStartLocation().getLongitude()).isEqualByComparingTo(currentLng);
        assertThat(response.getDestination().getTitle()).isEqualTo("해운대 해수욕장");
        assertThat(response.getDestination().getAddr1()).isEqualTo("부산광역시 해운대구");
        assertThat(response.getDestination().getLatitude()).isEqualByComparingTo(reachLat);
    }

    @DisplayName("GO 시 Kafka 'user-go-log' 토픽에 올바른 메시지를 발행한다")
    @Test
    void startNavigation_sendsKafkaMessage() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;
        BigDecimal currentLat = new BigDecimal("37.5665");
        BigDecimal currentLng = new BigDecimal("126.9780");
        BigDecimal reachLat = new BigDecimal("35.1587");
        BigDecimal reachLng = new BigDecimal("129.1604");

        User user = createTestUser();
        user.completeOnboarding("US", 25, "male", "sightseeing", "active");

        Attraction attraction = createTestAttraction(reachLat, reachLng);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(attractionRepository.findById(placeId)).willReturn(Optional.of(attraction));

        // when
        guideService.startNavigation(userId, placeId, currentLat, currentLng, reachLat, reachLng);

        // then - Kafka 메시지 캡처 및 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("user-go-log"), messageCaptor.capture());

        String sentJson = messageCaptor.getValue();
        UserGoLogMessage sentMessage = objectMapper.readValue(sentJson, UserGoLogMessage.class);

        assertThat(sentMessage.getAction()).isEqualTo("GO");
        assertThat(sentMessage.getPlaceId()).isEqualTo(placeId);
        assertThat(sentMessage.getNationality()).isEqualTo("US");
        assertThat(sentMessage.getAge()).isEqualTo(25);
        assertThat(sentMessage.getGender()).isEqualTo("male");
        assertThat(sentMessage.getCurrentLat()).isEqualByComparingTo(currentLat);
        assertThat(sentMessage.getCurrentLng()).isEqualByComparingTo(currentLng);
        assertThat(sentMessage.getReachLat()).isEqualByComparingTo(reachLat);
        assertThat(sentMessage.getReachLng()).isEqualByComparingTo(reachLng);
        assertThat(sentMessage.getTimestamp()).isNotNull();
    }

    @DisplayName("존재하지 않는 유저 ID로 GO 시 ResourceNotFoundException이 발생한다")
    @Test
    void startNavigation_userNotFound_throws() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> guideService.startNavigation(
                999L, 100L,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("존재하지 않는 관광지 ID로 GO 시 ResourceNotFoundException이 발생한다")
    @Test
    void startNavigation_attractionNotFound_throws() {
        // given
        User user = createTestUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(attractionRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> guideService.startNavigation(
                1L, 999L,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
