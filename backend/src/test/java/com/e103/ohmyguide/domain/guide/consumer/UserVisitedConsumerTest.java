package com.e103.ohmyguide.domain.guide.consumer;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attractionvector.entity.AttractionVector;
import com.e103.ohmyguide.domain.attractionvector.repository.AttractionVectorRepository;
import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.guide.consumer.userVisited.UserVisitedConsumer;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.uservector.entity.UserVector;
import com.e103.ohmyguide.domain.uservector.repository.UserVectorRepository;
import com.e103.ohmyguide.domain.uservisited.entity.UserVisited;
import com.e103.ohmyguide.domain.uservisited.repository.UserVisitedRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserVisitedConsumerTest {

    @InjectMocks
    private UserVisitedConsumer consumer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttractionRepository attractionRepository;

    @Mock
    private UserVisitedRepository userVisitedRepository;

    @Mock
    private UserVectorRepository userVectorRepository;

    @Mock
    private AttractionVectorRepository attractionVectorRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("GO 메시지 소비 시 방문 기록(UserVisited)을 저장한다")
    @Test
    void consume_savesUserVisited() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;

        User user = createTestUser();
        Attraction attraction = createTestAttraction();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(attractionRepository.findById(placeId)).willReturn(Optional.of(attraction));
        given(userVisitedRepository.existsByUserAndAttraction(user, attraction)).willReturn(false);

        String kafkaMessage = createGoLogJson(userId, placeId);

        // when
        consumer.consume(kafkaMessage);

        // then
        ArgumentCaptor<UserVisited> captor = ArgumentCaptor.forClass(UserVisited.class);
        verify(userVisitedRepository).save(captor.capture());

        UserVisited saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getAttraction()).isEqualTo(attraction);
    }

    @DisplayName("이미 방문한 관광지는 중복 저장하지 않는다")
    @Test
    void consume_alreadyVisited_doesNotDuplicate() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;

        User user = createTestUser();
        Attraction attraction = createTestAttraction();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(attractionRepository.findById(placeId)).willReturn(Optional.of(attraction));
        given(userVisitedRepository.existsByUserAndAttraction(user, attraction)).willReturn(true);

        // when
        consumer.consume(createGoLogJson(userId, placeId));

        // then
        verify(userVisitedRepository, never()).save(any());
    }

    @DisplayName("GO 메시지 소비 시 유저 선호 벡터를 업데이트한다 (학습률 0.15)")
    @Test
    void consume_updatesUserVector() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;

        User user = createTestUser();
        Attraction attraction = createTestAttraction();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(attractionRepository.findById(placeId)).willReturn(Optional.of(attraction));
        given(userVisitedRepository.existsByUserAndAttraction(user, attraction)).willReturn(false);

        // 유저 벡터: [1.0, 0.0, 0.5]
        UserVector userVector = mock(UserVector.class);
        given(userVector.getPreferenceVector()).willReturn("[1.0, 0.0, 0.5]");
        given(userVectorRepository.findById(userId)).willReturn(Optional.of(userVector));

        // 관광지 벡터: [0.0, 1.0, 1.0]
        AttractionVector attractionVector = mock(AttractionVector.class);
        given(attractionVector.getAttractionVector()).willReturn("[0.0, 1.0, 1.0]");
        given(attractionVectorRepository.findById(placeId)).willReturn(Optional.of(attractionVector));

        // when
        consumer.consume(createGoLogJson(userId, placeId));

        // then - newVec = uVec + 0.15 * (aVec - uVec)
        // [1.0 + 0.15*(0.0-1.0), 0.0 + 0.15*(1.0-0.0), 0.5 + 0.15*(1.0-0.5)]
        // = [0.85, 0.15, 0.575]
        ArgumentCaptor<String> vectorCaptor = ArgumentCaptor.forClass(String.class);
        verify(userVector).updatePreferenceVector(vectorCaptor.capture());

        String updatedJson = vectorCaptor.getValue();
        java.util.List<?> updated = objectMapper.readValue(updatedJson, java.util.List.class);
        assertThat(updated).hasSize(3);
        assertThat(((Number) updated.get(0)).doubleValue()).isCloseTo(0.85, org.assertj.core.data.Offset.offset(0.001));
        assertThat(((Number) updated.get(1)).doubleValue()).isCloseTo(0.15, org.assertj.core.data.Offset.offset(0.001));
        assertThat(((Number) updated.get(2)).doubleValue()).isCloseTo(0.575, org.assertj.core.data.Offset.offset(0.001));
    }

    @DisplayName("유저가 존재하지 않으면 무시한다")
    @Test
    void consume_userNotFound_skips() throws Exception {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when
        consumer.consume(createGoLogJson(999L, 100L));

        // then
        verify(userVisitedRepository, never()).save(any());
        verify(userVectorRepository, never()).findById(any());
    }

    @DisplayName("관광지가 존재하지 않으면 무시한다")
    @Test
    void consume_attractionNotFound_skips() throws Exception {
        // given
        User user = createTestUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(attractionRepository.findById(999L)).willReturn(Optional.empty());

        // when
        consumer.consume(createGoLogJson(1L, 999L));

        // then
        verify(userVisitedRepository, never()).save(any());
    }

    @DisplayName("잘못된 JSON은 무시한다")
    @Test
    void consume_invalidJson_skips() {
        // when
        consumer.consume("broken json {{{}");

        // then
        verify(userRepository, never()).findById(any());
    }

    // -- 헬퍼 --

    private User createTestUser() {
        return User.oauth2Builder()
                .email("test@gmail.com")
                .name("TestUser")
                .provider(AuthProvider.google)
                .providerId("google-123")
                .build();
    }

    private Attraction createTestAttraction() {
        return Attraction.builder()
                .title("경복궁")
                .latitude(new BigDecimal("37.58"))
                .longitude(new BigDecimal("126.97"))
                .build();
    }

    private String createGoLogJson(Long userId, Long placeId) throws Exception {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", userId);
        map.put("nationality", "US");
        map.put("age", 25);
        map.put("gender", "male");
        map.put("travelPurpose", "sightseeing");
        map.put("lifestyle", "active");
        map.put("action", "GO");
        map.put("placeId", placeId);
        map.put("currentLat", new BigDecimal("37.50"));
        map.put("currentLng", new BigDecimal("127.00"));
        map.put("reachLat", new BigDecimal("37.55"));
        map.put("reachLng", new BigDecimal("127.05"));
        map.put("timestamp", "2026-03-26T10:00:00");
        return objectMapper.writeValueAsString(map);
    }
}
