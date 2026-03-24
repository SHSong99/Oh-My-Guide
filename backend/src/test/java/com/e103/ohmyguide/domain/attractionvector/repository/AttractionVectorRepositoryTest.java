package com.e103.ohmyguide.domain.attractionvector.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attractionvector.entity.AttractionVector;
import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import com.e103.ohmyguide.domain.contenttype.repository.ContentTypeRepository;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.domain.sido.repository.SidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AttractionVectorRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AttractionVectorRepository attractionVectorRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private SidoRepository sidoRepository;

    @Autowired
    private ContentTypeRepository contentTypeRepository;

    private Attraction createAttraction() {
        Sido sido = sidoRepository.save(Sido.builder().sidoCode(1).sidoName("서울").build());
        ContentType contentType = contentTypeRepository.save(
                ContentType.builder().contentTypeId(12L).contentTypeName("관광지").build());
        return attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .build());
    }

    @DisplayName("AttractionVector 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        Attraction attraction = createAttraction();

        AttractionVector vector = AttractionVector.builder()
                .attraction(attraction)
                .attractionVector("[0.2, 0.4, 0.6]")
                .build();

        // when
        AttractionVector saved = attractionVectorRepository.save(vector);
        Optional<AttractionVector> found = attractionVectorRepository.findById(saved.getAttrId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAttractionVector()).isEqualTo("[0.2, 0.4, 0.6]");
    }

    @DisplayName("AttractionVector 의 PK 는 Attraction 의 ID 와 동일하다. (1:1 관계 검증)")
    @Test
    void attractionVectorPkEqualsAttractionId() {
        // given
        Attraction attraction = createAttraction();

        AttractionVector saved = attractionVectorRepository.save(AttractionVector.builder()
                .attraction(attraction)
                .attractionVector("[0.2, 0.4, 0.6]")
                .build());

        // then
        assertThat(saved.getAttrId()).isEqualTo(attraction.getId());
    }

    @DisplayName("AttractionVector 조회 시 연관된 Attraction 을 함께 확인한다.")
    @Test
    void findWithAttractionRelation() {
        // given
        Attraction attraction = createAttraction();

        attractionVectorRepository.save(AttractionVector.builder()
                .attraction(attraction)
                .attractionVector("[0.2, 0.4, 0.6]")
                .build());

        // when
        AttractionVector found = attractionVectorRepository.findById(attraction.getId()).orElseThrow();

        // then
        assertThat(found.getAttraction().getTitle()).isEqualTo("경복궁");
    }

    @DisplayName("attractionVector 를 업데이트한다.")
    @Test
    void updateAttractionVector() {
        // given
        Attraction attraction = createAttraction();

        AttractionVector vector = attractionVectorRepository.save(AttractionVector.builder()
                .attraction(attraction)
                .attractionVector("[0.2, 0.4, 0.6]")
                .build());

        // when
        vector.updateAttractionVector("[0.9, 0.1, 0.5]");

        // then
        assertThat(vector.getAttractionVector()).isEqualTo("[0.9, 0.1, 0.5]");
    }
}
