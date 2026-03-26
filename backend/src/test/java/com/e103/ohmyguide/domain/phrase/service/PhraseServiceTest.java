package com.e103.ohmyguide.domain.phrase.service;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.auth.oauth2.AuthProvider;
import com.e103.ohmyguide.domain.phrase.entity.Phrase;
import com.e103.ohmyguide.domain.phrase.entity.PhraseLanguage;
import com.e103.ohmyguide.domain.phrase.repository.PhraseRepository;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.userphrase.entity.UserPhrase;
import com.e103.ohmyguide.domain.userphrase.repository.UserPhraseRepository;
import com.e103.ohmyguide.global.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.e103.ohmyguide.domain.phrase.dto.PhraseResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhraseServiceTest extends IntegrationTestSupport {

    @Autowired
    private PhraseService phraseService;

    @Autowired
    private PhraseRepository phraseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPhraseRepository userPhraseRepository;

    @DisplayName("사용자의 북마크 목록을 조회한다.")
    @Test
    void getBookmarks_returnsBookmarkedPhrases() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Phrase phrase1 = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        Phrase phrase2 = buildPhrase("감사합니다", PhraseLanguage.KOR);
        phraseRepository.save(phrase1);
        phraseRepository.save(phrase2);

        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase1).build());
        userPhraseRepository.save(UserPhrase.builder().user(user).phrase(phrase2).build());

        // when
        List<PhraseResponse> bookmarks = phraseService.getBookmarks(user.getId());

        // then
        assertThat(bookmarks).hasSize(2);
        assertThat(bookmarks).extracting(PhraseResponse::getContent)
                .containsExactlyInAnyOrder("안녕하세요", "감사합니다");
    }

    @DisplayName("북마크가 없는 사용자는 빈 목록을 반환한다.")
    @Test
    void getBookmarks_noBookmarks_returnsEmptyList() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        // when
        List<PhraseResponse> bookmarks = phraseService.getBookmarks(user.getId());

        // then
        assertThat(bookmarks).isEmpty();
    }

    @DisplayName("존재하지 않는 사용자 ID로 북마크 조회 시 예외가 발생한다.")
    @Test
    void getBookmarks_userNotFound_throwsException() {
        // given
        Long invalidUserId = 999L;

        // when & then
        assertThatThrownBy(() -> phraseService.getBookmarks(invalidUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @DisplayName("사용자가 문구를 북마크에 추가한다.")
    @Test
    void addBookmark_savesUserPhrase() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        // when
        phraseService.addBookmark(phrase.getId(), user.getId());

        // then
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, phrase)).isTrue();
    }

    @DisplayName("이미 북마크된 문구를 다시 추가하면 무시된다.")
    @Test
    void addBookmark_duplicateBookmark_ignored() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        UserPhrase existingBookmark = UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build();
        userPhraseRepository.save(existingBookmark);

        long countBefore = userPhraseRepository.count();

        // when
        phraseService.addBookmark(phrase.getId(), user.getId());

        // then
        long countAfter = userPhraseRepository.count();
        assertThat(countAfter).isEqualTo(countBefore);
    }

    @DisplayName("존재하지 않는 문구 ID로 북마크 추가 시 예외가 발생한다.")
    @Test
    void addBookmark_phraseNotFound_throwsException() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Long invalidPhraseId = 999L;

        // when & then
        assertThatThrownBy(() -> phraseService.addBookmark(invalidPhraseId, user.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phrase not found");
    }

    @DisplayName("존재하지 않는 사용자 ID로 북마크 추가 시 예외가 발생한다.")
    @Test
    void addBookmark_userNotFound_throwsException() {
        // given
        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        Long invalidUserId = 999L;

        // when & then
        assertThatThrownBy(() -> phraseService.addBookmark(phrase.getId(), invalidUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @DisplayName("사용자가 북마크를 삭제한다.")
    @Test
    void removeBookmark_deletesUserPhrase() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        UserPhrase bookmark = UserPhrase.builder()
                .user(user)
                .phrase(phrase)
                .build();
        userPhraseRepository.save(bookmark);

        // when
        phraseService.removeBookmark(phrase.getId(), user.getId());

        // then
        assertThat(userPhraseRepository.existsByUserAndPhrase(user, phrase)).isFalse();
    }

    @DisplayName("존재하지 않는 북마크를 삭제해도 예외가 발생하지 않는다.")
    @Test
    void removeBookmark_nonExistentBookmark_noException() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        // when & then (예외 발생하지 않음)
        phraseService.removeBookmark(phrase.getId(), user.getId());
    }

    @DisplayName("존재하지 않는 문구 ID로 북마크 삭제 시 예외가 발생한다.")
    @Test
    void removeBookmark_phraseNotFound_throwsException() {
        // given
        User user = buildUser("test@example.com", "Test User");
        userRepository.save(user);

        Long invalidPhraseId = 999L;

        // when & then
        assertThatThrownBy(() -> phraseService.removeBookmark(invalidPhraseId, user.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Phrase not found");
    }

    @DisplayName("존재하지 않는 사용자 ID로 북마크 삭제 시 예외가 발생한다.")
    @Test
    void removeBookmark_userNotFound_throwsException() {
        // given
        Phrase phrase = buildPhrase("안녕하세요", PhraseLanguage.KOR);
        phraseRepository.save(phrase);

        Long invalidUserId = 999L;

        // when & then
        assertThatThrownBy(() -> phraseService.removeBookmark(phrase.getId(), invalidUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    private User buildUser(String email, String name) {
        return User.oauth2Builder()
                .email(email)
                .name(name)
                .imageUrl("https://example.com/image.jpg")
                .provider(AuthProvider.google)
                .providerId("google123")
                .build();
    }

    private Phrase buildPhrase(String content, PhraseLanguage language) {
        return Phrase.builder()
                .content(content)
                .language(language)
                .build();
    }
}
