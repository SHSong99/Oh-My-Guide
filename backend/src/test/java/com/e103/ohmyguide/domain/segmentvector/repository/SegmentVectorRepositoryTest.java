package com.e103.ohmyguide.domain.segmentvector.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.segmentvector.entity.SegmentVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentVectorRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SegmentVectorRepository segmentVectorRepository;

    @DisplayName("SegmentVector를 저장하고 ID로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        SegmentVector vector = SegmentVector.builder()
                .segmentType("nationality")
                .segmentKey("Japan")
                .segmentVector("{\"nature\": 0.73, \"food\": 0.68}")
                .source("csv")
                .sampleCount(102)
                .build();

        // when
        SegmentVector saved = segmentVectorRepository.save(vector);
        Optional<SegmentVector> found = segmentVectorRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSegmentType()).isEqualTo("nationality");
        assertThat(found.get().getSegmentKey()).isEqualTo("Japan");
        assertThat(found.get().getSegmentVector()).contains("nature");
    }

    @DisplayName("segmentType과 segmentKey로 조회한다.")
    @Test
    void findBySegmentTypeAndSegmentKey() {
        // given
        segmentVectorRepository.save(SegmentVector.builder()
                .segmentType("age")
                .segmentKey("20s")
                .segmentVector("{\"food\": 0.75, \"active\": 0.73}")
                .sampleCount(338)
                .build());

        // when
        Optional<SegmentVector> found = segmentVectorRepository
                .findBySegmentTypeAndSegmentKey("age", "20s");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSegmentKey()).isEqualTo("20s");
        assertThat(found.get().getSampleCount()).isEqualTo(338);
    }

    @DisplayName("segmentType으로 목록을 조회한다.")
    @Test
    void findBySegmentType() {
        // given
        segmentVectorRepository.save(SegmentVector.builder()
                .segmentType("gender")
                .segmentKey("male")
                .segmentVector("{\"active\": 0.73}")
                .sampleCount(471)
                .build());
        segmentVectorRepository.save(SegmentVector.builder()
                .segmentType("gender")
                .segmentKey("female")
                .segmentVector("{\"nature\": 0.70}")
                .sampleCount(589)
                .build());

        // when
        List<SegmentVector> genderVectors = segmentVectorRepository.findBySegmentType("gender");

        // then
        assertThat(genderVectors).hasSize(2);
        assertThat(genderVectors).extracting(SegmentVector::getSegmentKey)
                .containsExactlyInAnyOrder("male", "female");
    }

    @DisplayName("존재하지 않는 segmentType/segmentKey 조회 시 빈 Optional을 반환한다.")
    @Test
    void findBySegmentTypeAndSegmentKey_notFound() {
        // when
        Optional<SegmentVector> found = segmentVectorRepository
                .findBySegmentTypeAndSegmentKey("nationality", "Brazil");

        // then
        assertThat(found).isEmpty();
    }

    @DisplayName("source 기본값은 csv이다.")
    @Test
    void defaultSourceIsCsv() {
        // given
        SegmentVector saved = segmentVectorRepository.save(SegmentVector.builder()
                .segmentType("nationality")
                .segmentKey("China")
                .segmentVector("{\"shopping\": 0.72}")
                .build());

        // then
        assertThat(saved.getSource()).isEqualTo("csv");
        assertThat(saved.getSampleCount()).isEqualTo(0);
    }

    @DisplayName("UNIQUE 제약조건: 동일한 (segmentType, segmentKey) 저장 시 예외가 발생한다.")
    @Test
    void uniqueConstraintViolation() {
        // given
        segmentVectorRepository.saveAndFlush(SegmentVector.builder()
                .segmentType("age")
                .segmentKey("30s")
                .segmentVector("{\"food\": 0.62}")
                .build());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.dao.DataIntegrityViolationException.class,
                () -> segmentVectorRepository.saveAndFlush(SegmentVector.builder()
                        .segmentType("age")
                        .segmentKey("30s")
                        .segmentVector("{\"nature\": 0.69}")
                        .build())
        );
    }
}
