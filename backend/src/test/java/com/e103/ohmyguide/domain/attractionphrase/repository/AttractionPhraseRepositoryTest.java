package com.e103.ohmyguide.domain.attractionphrase.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.attraction.entity.Attraction;
import com.e103.ohmyguide.domain.attraction.repository.AttractionRepository;
import com.e103.ohmyguide.domain.attractionphrase.entity.AttractionPhrase;
import com.e103.ohmyguide.domain.contenttype.entity.ContentType;
import com.e103.ohmyguide.domain.contenttype.repository.ContentTypeRepository;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.entity.PhraseLanguage;
import com.e103.ohmyguide.domain.phrase.repository.PhraseRepository;
import com.e103.ohmyguide.domain.sido.entity.Sido;
import com.e103.ohmyguide.domain.sido.repository.SidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttractionPhraseRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private AttractionPhraseRepository attractionPhraseRepository;

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private PhraseRepository phraseRepository;

    @Autowired
    private SidoRepository sidoRepository;

    @Autowired
    private ContentTypeRepository contentTypeRepository;

    private Attraction attraction;
    private Phrase phrase;

    @BeforeEach
    void setUp() {
        Sido sido = sidoRepository.save(Sido.builder().sidoCode(1).sidoName("서울").build());
        ContentType contentType = contentTypeRepository.save(
                ContentType.builder().contentTypeId(12L).contentTypeName("관광지").build());

        attraction = attractionRepository.save(Attraction.builder()
                .title("경복궁")
                .sido(sido)
                .contentType(contentType)
                .build());

        phrase = phraseRepository.save(Phrase.builder()
                .content("화장실이 어디예요?")
                .language(PhraseLanguage.KOR)
                .build());
    }

    @DisplayName("AttractionPhrase 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        AttractionPhrase attractionPhrase = AttractionPhrase.builder()
                .attraction(attraction)
                .phrase(phrase)
                .build();

        // when
        AttractionPhrase saved = attractionPhraseRepository.save(attractionPhrase);
        Optional<AttractionPhrase> found = attractionPhraseRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getAttraction().getTitle()).isEqualTo("경복궁");
        assertThat(found.get().getPhrase().getContent()).isEqualTo("화장실이 어디예요?");
    }

    @DisplayName("하나의 Attraction 에 여러 Phrase 를 연결할 수 있다.")
    @Test
    void attractionCanHaveMultiplePhrases() {
        // given
        Phrase phrase2 = phraseRepository.save(Phrase.builder()
                .content("입장료가 얼마예요?")
                .language(PhraseLanguage.KOR)
                .build());

        attractionPhraseRepository.save(AttractionPhrase.builder().attraction(attraction).phrase(phrase).build());
        attractionPhraseRepository.save(AttractionPhrase.builder().attraction(attraction).phrase(phrase2).build());

        // when
        List<AttractionPhrase> all = attractionPhraseRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(ap -> ap.getAttraction().getId())
                .containsOnly(attraction.getId());
    }

    @DisplayName("동일한 (attraction, phrase) 조합으로 중복 저장 시 예외가 발생한다.")
    @Test
    void duplicateAttractionPhraseThrowsException() {
        // given
        attractionPhraseRepository.saveAndFlush(
                AttractionPhrase.builder().attraction(attraction).phrase(phrase).build());

        // when & then
        assertThatThrownBy(() ->
                attractionPhraseRepository.saveAndFlush(
                        AttractionPhrase.builder().attraction(attraction).phrase(phrase).build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("AttractionPhrase 를 삭제한다.")
    @Test
    void delete() {
        // given
        AttractionPhrase saved = attractionPhraseRepository.save(
                AttractionPhrase.builder().attraction(attraction).phrase(phrase).build());

        // when
        attractionPhraseRepository.deleteById(saved.getId());

        // then
        assertThat(attractionPhraseRepository.findById(saved.getId())).isEmpty();
    }
}
