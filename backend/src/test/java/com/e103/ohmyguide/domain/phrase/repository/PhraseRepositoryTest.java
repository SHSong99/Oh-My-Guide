package com.e103.ohmyguide.domain.phrase.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.entity.PhraseLanguage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PhraseRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private PhraseRepository phraseRepository;

    @DisplayName("KOR Phrase 를 저장하고 조회한다.")
    @Test
    void saveAndFindKorPhrase() {
        // given
        Phrase phrase = Phrase.builder()
                .content("화장실이 어디예요?")
                .language(PhraseLanguage.KOR)
                .build();

        // when
        Phrase saved = phraseRepository.save(phrase);
        Optional<Phrase> found = phraseRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("화장실이 어디예요?");
        assertThat(found.get().getLanguage()).isEqualTo(PhraseLanguage.KOR);
    }

    @DisplayName("ENG Phrase 를 저장하고 조회한다.")
    @Test
    void saveAndFindEngPhrase() {
        // given
        Phrase phrase = Phrase.builder()
                .content("Where is the restroom?")
                .language(PhraseLanguage.ENG)
                .build();

        // when
        Phrase saved = phraseRepository.save(phrase);
        Optional<Phrase> found = phraseRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Where is the restroom?");
        assertThat(found.get().getLanguage()).isEqualTo(PhraseLanguage.ENG);
    }

    @DisplayName("KOR/ENG Phrase 를 저장하고 전체 조회한다.")
    @Test
    void saveAllAndFindAll() {
        // given
        phraseRepository.save(Phrase.builder().content("화장실이 어디예요?").language(PhraseLanguage.KOR).build());
        phraseRepository.save(Phrase.builder().content("입장료가 얼마예요?").language(PhraseLanguage.KOR).build());
        phraseRepository.save(Phrase.builder().content("Where is the restroom?").language(PhraseLanguage.ENG).build());

        // when
        List<Phrase> all = phraseRepository.findAll();

        // then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Phrase::getLanguage)
                .containsExactlyInAnyOrder(PhraseLanguage.KOR, PhraseLanguage.KOR, PhraseLanguage.ENG);
    }

    @DisplayName("Phrase 를 삭제한다.")
    @Test
    void delete() {
        // given
        Phrase phrase = phraseRepository.save(Phrase.builder()
                .content("화장실이 어디예요?")
                .language(PhraseLanguage.KOR)
                .build());

        // when
        phraseRepository.deleteById(phrase.getId());

        // then
        assertThat(phraseRepository.findById(phrase.getId())).isEmpty();
    }
}
