package com.e103.ohmyguide.domain.attraction.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import com.e103.ohmyguide.domain.contenttype.repository.ContentTypeRepository;
import com.e103.ohmyguide.domain.gugun.entity.Gugun;
import com.e103.ohmyguide.domain.gugun.repository.GugunRepository;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.domain.sido.repository.SidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AttractionRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private SidoRepository sidoRepository;

    @Autowired
    private GugunRepository gugunRepository;

    @Autowired
    private ContentTypeRepository contentTypeRepository;

    private Sido sido;
    private Gugun gugun;
    private ContentType contentType;

    @BeforeEach
    void setUp() {
        sido = sidoRepository.save(Sido.builder()
                .sidoCode(1)
                .sidoName("서울")
                .build());

        gugun = gugunRepository.save(Gugun.builder()
                .sido(sido)
                .gugunCode(1)
                .gugunName("종로구")
                .build());

        contentType = contentTypeRepository.save(ContentType.builder()
                .contentTypeId(12L)
                .contentTypeName("관광지")
                .build());
    }

    @DisplayName("Attraction 을 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        Attraction attraction = Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .addr1("서울특별시 종로구 사직로 161")
                .build();

        // when
        Attraction saved = attractionRepository.save(attraction);
        Optional<Attraction> found = attractionRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("경복궁");
        assertThat(found.get().getAddr1()).isEqualTo("서울특별시 종로구 사직로 161");
    }

    @DisplayName("Attraction 조회 시 연관된 Sido 를 확인한다.")
    @Test
    void findWithSidoRelation() {
        // given
        Attraction saved = attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .build());

        // when
        Attraction found = attractionRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getSido().getSidoCode()).isEqualTo(1);
        assertThat(found.getSido().getSidoName()).isEqualTo("서울");
    }

    @DisplayName("Attraction 조회 시 연관된 ContentType 을 확인한다.")
    @Test
    void findWithContentTypeRelation() {
        // given
        Attraction saved = attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .build());

        // when
        Attraction found = attractionRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getContentType().getContentTypeId()).isEqualTo(12L);
        assertThat(found.getContentType().getContentTypeName()).isEqualTo("관광지");
    }

    @DisplayName("Attraction 저장 시 Gugun 연관관계를 함께 설정한다.")
    @Test
    void saveWithGugunRelation() {
        // given
        Attraction saved = attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .gugunCode(gugun.getGugunCode())
                .gugun(gugun)
                .contentType(contentType)
                .build());

        // when
        Attraction found = attractionRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getGugunCode()).isEqualTo(1);
        assertThat(found.getGugun().getGugunName()).isEqualTo("종로구");
    }

    @DisplayName("Attraction 을 삭제한다.")
    @Test
    void delete() {
        // given
        Attraction saved = attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .build());

        // when
        attractionRepository.deleteById(saved.getId());

        // then
        assertThat(attractionRepository.findById(saved.getId())).isEmpty();
    }
}
