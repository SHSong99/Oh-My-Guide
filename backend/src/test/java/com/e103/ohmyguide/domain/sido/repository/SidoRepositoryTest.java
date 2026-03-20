package com.e103.ohmyguide.domain.sido.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SidoRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private SidoRepository sidoRepository;

    @DisplayName("Sido 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        Sido sido = Sido.builder()
                .sidoCode(1)
                .sidoName("서울")
                .build();

        // when
        Sido saved = sidoRepository.save(sido);
        Optional<Sido> found = sidoRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSidoCode()).isEqualTo(1);
        assertThat(found.get().getSidoName()).isEqualTo("서울");
    }

    @DisplayName("여러 Sido 를 저장하고 전체 조회한다.")
    @Test
    void saveAllAndFindAll() {
        // given
        sidoRepository.save(Sido.builder().sidoCode(1).sidoName("서울").build());
        sidoRepository.save(Sido.builder().sidoCode(6).sidoName("부산").build());
        sidoRepository.save(Sido.builder().sidoCode(31).sidoName("경기도").build());

        // when
        List<Sido> sidos = sidoRepository.findAll();

        // then
        assertThat(sidos).hasSize(3)
                .extracting(Sido::getSidoName)
                .containsExactlyInAnyOrder("서울", "부산", "경기도");
    }

    @DisplayName("Sido 를 삭제한다.")
    @Test
    void delete() {
        // given
        Sido sido = sidoRepository.save(Sido.builder()
                .sidoCode(1)
                .sidoName("서울")
                .build());

        // when
        sidoRepository.deleteById(sido.getId());

        // then
        assertThat(sidoRepository.findById(sido.getId())).isEmpty();
    }
}
