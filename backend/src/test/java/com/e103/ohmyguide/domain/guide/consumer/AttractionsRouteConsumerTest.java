package com.e103.ohmyguide.domain.guide.consumer;

import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.guide.consumer.attractionsRoute.AttractionsRouteConsumer;
import com.e103.ohmyguide.domain.guide.dto.AttractionsRouteResponseMessage;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AttractionsRouteConsumerTest {

    @InjectMocks
    private AttractionsRouteConsumer consumer;

    @Mock
    private AttractionRepository attractionRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @DisplayName("Kafka 메시지를 소비하면 bounding box 내 주변 관광지를 SSE로 전송한다")
    @Test
    void consume_sendsNearbyPlacesViaSse() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;
        BigDecimal currentLat = new BigDecimal("37.50");
        BigDecimal currentLng = new BigDecimal("127.00");
        BigDecimal reachLat = new BigDecimal("37.55");
        BigDecimal reachLng = new BigDecimal("127.05");

        String kafkaMessage = objectMapper.writeValueAsString(
                createKafkaMessage(userId, placeId, currentLat, currentLng, reachLat, reachLng));

        Attraction destination = createAttraction(100L, "경복궁", reachLat, reachLng);
        Attraction nearby1 = createAttraction(201L, "북촌한옥마을", new BigDecimal("37.52"), new BigDecimal("127.02"));
        Attraction nearby2 = createAttraction(202L, "인사동", new BigDecimal("37.53"), new BigDecimal("127.01"));

        given(attractionRepository.findById(placeId)).willReturn(Optional.of(destination));
        given(attractionRepository.findWithinBoundingBox(
                eq(currentLat), eq(reachLat), eq(currentLng), eq(reachLng), eq(placeId)))
                .willReturn(List.of(nearby1, nearby2));

        // when
        consumer.consume(kafkaMessage);

        // then - Kafka reply topic으로 발행된 응답 검증
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("attractions-route-response"), messageCaptor.capture());

        AttractionsRouteResponseMessage responseMessage =
                objectMapper.readValue(messageCaptor.getValue(), AttractionsRouteResponseMessage.class);
        assertThat(responseMessage.getUserId()).isEqualTo(userId);
        assertThat(responseMessage.getNavigationResponse().getStartLocation().getLatitude())
                .isEqualByComparingTo(currentLat);
        assertThat(responseMessage.getNavigationResponse().getDestination().getTitle()).isEqualTo("경복궁");
        assertThat(responseMessage.getNavigationResponse().getNearbyPlaces()).hasSize(2);
        assertThat(responseMessage.getNavigationResponse().getNearbyPlaces())
                .extracting("title")
                .containsExactly("북촌한옥마을", "인사동");
    }

    @DisplayName("bounding box 내 주변 관광지가 없으면 빈 리스트로 SSE 전송한다")
    @Test
    void consume_noNearbyPlaces_sendsEmptyList() throws Exception {
        // given
        Long userId = 1L;
        Long placeId = 100L;
        BigDecimal currentLat = new BigDecimal("37.50");
        BigDecimal currentLng = new BigDecimal("127.00");
        BigDecimal reachLat = new BigDecimal("37.55");
        BigDecimal reachLng = new BigDecimal("127.05");

        String kafkaMessage = objectMapper.writeValueAsString(
                createKafkaMessage(userId, placeId, currentLat, currentLng, reachLat, reachLng));

        Attraction destination = createAttraction(100L, "외진곳", reachLat, reachLng);

        given(attractionRepository.findById(placeId)).willReturn(Optional.of(destination));
        given(attractionRepository.findWithinBoundingBox(any(), any(), any(), any(), any()))
                .willReturn(List.of());

        // when
        consumer.consume(kafkaMessage);

        // then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("attractions-route-response"), messageCaptor.capture());

        AttractionsRouteResponseMessage responseMessage =
                objectMapper.readValue(messageCaptor.getValue(), AttractionsRouteResponseMessage.class);
        assertThat(responseMessage.getNavigationResponse().getNearbyPlaces()).isEmpty();
    }

    @DisplayName("잘못된 JSON 메시지는 무시하고 SSE 전송하지 않는다")
    @Test
    void consume_invalidJson_doesNotSendSse() {
        // when
        consumer.consume("{ invalid json }}}");

        // then
        verify(kafkaTemplate, never()).send(any(), any());
    }

    // -- 헬퍼 메서드 --

    private java.util.Map<String, Object> createKafkaMessage(
            Long userId, Long placeId,
            BigDecimal currentLat, BigDecimal currentLng,
            BigDecimal reachLat, BigDecimal reachLng) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("userId", userId);
        map.put("nationality", "US");
        map.put("age", 25);
        map.put("gender", "male");
        map.put("travelPurpose", "sightseeing");
        map.put("lifestyle", "active");
        map.put("action", "GO");
        map.put("placeId", placeId);
        map.put("currentLat", currentLat);
        map.put("currentLng", currentLng);
        map.put("reachLat", reachLat);
        map.put("reachLng", reachLng);
        return map;
    }

    private Attraction createAttraction(Long id, String title, BigDecimal lat, BigDecimal lng) {
        return Attraction.builder()
                .title(title)
                .addr1("서울특별시")
                .latitude(lat)
                .longitude(lng)
                .overview(title + " 소개")
                .build();
    }
}
