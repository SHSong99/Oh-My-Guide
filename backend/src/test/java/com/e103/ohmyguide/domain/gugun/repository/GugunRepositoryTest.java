package com.e103.ohmyguide.domain.gugun.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.gugun.entity.Gugun;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.domain.sido.repository.SidoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class GugunRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private GugunRepository gugunRepository;

    @Autowired
    private SidoRepository sidoRepository;

    @DisplayName("Gugun 을 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        Sido sido = sidoRepository.save(Sido.builder()
                .sidoCode(1)
                .sidoName("서울")
                .build());

        Gugun gugun = Gugun.builder()
                .sido(sido)
                .gugunCode(1)
                .gugunName("강남구")
                .build();

        // when
        Gugun saved = gugunRepository.save(gugun);
        Optional<Gugun> found = gugunRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getGugunName()).isEqualTo("강남구");
        assertThat(found.get().getGugunCode()).isEqualTo(1);
    }

    @DisplayName("Gugun 조회 시 연관된 Sido 를 함께 확인한다.")
    @Test
    void findWithSidoRelation() {
        // given
        Sido sido = sidoRepository.save(Sido.builder()
                .sidoCode(1)
                .sidoName("서울")
                .build());

        Gugun gugun = gugunRepository.save(Gugun.builder()
                .sido(sido)
                .gugunCode(1)
                .gugunName("강남구")
                .build());

        // when
        Gugun found = gugunRepository.findById(gugun.getId()).orElseThrow();

        // then
        assertThat(found.getSido().getSidoCode()).isEqualTo(1);
        assertThat(found.getSido().getSidoName()).isEqualTo("서울");
    }

    @DisplayName("같은 gugun_code 라도 sido 가 다르면 별개의 Gugun 으로 저장된다. (복합 UNIQUE 검증)")
    @Test
    void sameGugunCodeWithDifferentSido() {
        // given
        Sido seoul = sidoRepository.save(Sido.builder().sidoCode(1).sidoName("서울").build());
        Sido incheon = sidoRepository.save(Sido.builder().sidoCode(2).sidoName("인천").build());

        // when
        Gugun seoulGugun = gugunRepository.save(Gugun.builder()
                .sido(seoul).gugunCode(1).gugunName("강남구").build());
        Gugun incheonGugun = gugunRepository.save(Gugun.builder()
                .sido(incheon).gugunCode(1).gugunName("강화군").build());

        // then
        List<Gugun> all = gugunRepository.findAll();
        assertThat(all).hasSize(2);
        assertThat(seoulGugun.getGugunCode()).isEqualTo(incheonGugun.getGugunCode());
        assertThat(seoulGugun.getSido().getSidoCode()).isNotEqualTo(incheonGugun.getSido().getSidoCode());
    }

    @DisplayName("Gugun 을 삭제한다.")
    @Test
    void delete() {
        // given
        Sido sido = sidoRepository.save(Sido.builder().sidoCode(1).sidoName("서울").build());
        Gugun gugun = gugunRepository.save(Gugun.builder()
                .sido(sido).gugunCode(1).gugunName("강남구").build());

        // when
        gugunRepository.deleteById(gugun.getId());

        // then
        assertThat(gugunRepository.findById(gugun.getId())).isEmpty();
    }
}
