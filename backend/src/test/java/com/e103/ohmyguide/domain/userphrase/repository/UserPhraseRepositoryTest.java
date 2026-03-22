package com.e103.ohmyguide.domain.userphrase.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.entity.PhraseLanguage;
import com.e103.ohmyguide.domain.phrase.repository.PhraseRepository;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.userphrase.entity.UserPhrase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserPhraseRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserPhraseRepository userPhraseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhraseRepository phraseRepository;

    private User user;
    private Phrase phrase;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.oauth2Builder()
                .email("test@test.com")
                .name("테스터")
                .imageUrl("https://image.url")
                .provider(AuthProvider.google)
                .providerId("google-id-123")
                .build());

        phrase = phraseRepository.save(Phrase.builder()
                .content("화장실이 어디예요?")
                .language(PhraseLanguage.KOR)
                .build());
    }

    @DisplayName("UserPhrase 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        UserPhrase userPhrase = UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build();

        // when
        UserPhrase saved = userPhraseRepository.save(userPhrase);
        Optional<UserPhrase> found = userPhraseRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getPhrase().getContent()).isEqualTo("화장실이 어디예요?");
    }

    @DisplayName("한 사용자가 여러 구문을 선택할 수 있다.")
    @Test
    void userCanSelectMultiplePhrases() {
        // given
        Phrase phrase2 = phraseRepository.save(Phrase.builder()
                .content("입장료가 얼마예요?")
                .language(PhraseLanguage.KOR)
                .build());

        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase).build());
        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase2).build());

        // when
        List<UserPhrase> all = userPhraseRepository.findAll();

        // then
        assertThat(all).hasSize(2);
        assertThat(all).extracting(up -> up.getUser().getId())
                .containsOnly(user.getId());
    }

    @DisplayName("동일한 (user, phrase) 조합으로 중복 저장 시 예외가 발생한다.")
    @Test
    void duplicateUserPhrasethrowsException() {
        // given
        userPhraseRepository.saveAndFlush(UserPhrase.builder().user(user).phrase(phrase).build());

        // when & then
        assertThatThrownBy(() ->
                userPhraseRepository.saveAndFlush(UserPhrase.builder().user(user).phrase(phrase).build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("UserPhrase 를 삭제한다.")
    @Test
    void delete() {
        // given
        UserPhrase saved = userPhraseRepository.save(UserPhrase.builder()
                .user(user).phrase(phrase).build());

        // when
        userPhraseRepository.deleteById(saved.getId());

        // then
        assertThat(userPhraseRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("User와 Phrase로 UserPhrase 존재 여부를 확인한다.")
    @Test
    void existsByUserAndPhrase() {
        // given
        userPhraseRepository.save(UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build());

        // when
        boolean exists = userPhraseRepository.existsByUserAndPhrase(user, phrase);

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("존재하지 않는 UserPhrase는 false를 반환한다.")
    @Test
    void existsByUserAndPhrase_notFound() {
        // when
        boolean exists = userPhraseRepository.existsByUserAndPhrase(user, phrase);

        // then
        assertThat(exists).isFalse();
    }

    @DisplayName("User와 Phrase로 UserPhrase를 삭제한다.")
    @Test
    void deleteByUserAndPhrase() {
        // given
        userPhraseRepository.save(UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build());

        // when
        userPhraseRepository.deleteByUserAndPhrase(user, phrase);

        // then
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, phrase)).isFalse();
    }

    @DisplayName("존재하지 않는 UserPhrase를 삭제해도 예외가 발생하지 않는다.")
    @Test
    void deleteByUserAndPhrase_notFound() {
        // when & then (예외 발생하지 않음)
        userPhraseRepository.deleteByUserAndPhrase(user, phrase);
    }

    @DisplayName("다른 User의 UserPhrase는 삭제되지 않는다.")
    @Test
    void deleteByUserAndPhrase_onlyDeletesSpecificUser() {
        // given
        User anotherUser = userRepository.save(User.oauth2Builder()
                .email("another@test.com")
                .name("다른사용자")
                .imageUrl("https://image.url")
                .provider(AuthProvider.google)
                .providerId("google-id-456")
                .build());

        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase).build());
        userPhraseRepository.save(UserPhrase.builder().user(anotherUser).phrase(phrase).build());

        // when
        userPhraseRepository.deleteByUserAndPhrase(user, phrase);

        // then
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, phrase)).isFalse();
        assertThat(userPhraseRepository.existsByUserAndPhrase(anotherUser, phrase)).isTrue();
    }

    @DisplayName("다른 Phrase의 UserPhrase는 삭제되지 않는다.")
    @Test
    void deleteByUserAndPhrase_onlyDeletesSpecificPhrase() {
        // given
        Phrase anotherPhrase = phraseRepository.save(Phrase.builder()
                .content("감사합니다")
                .language(PhraseLanguage.KOR)
                .build());

        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase).build());
        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(anotherPhrase).build());

        // when
        userPhraseRepository.deleteByUserAndPhrase(user, phrase);

        // then
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, phrase)).isFalse();
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, anotherPhrase)).isTrue();
    }
}
