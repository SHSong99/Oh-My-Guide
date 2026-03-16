package com.e103.ohmyguide.domain.user.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("User 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build();

        // when
        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@test.com");
        assertThat(found.get().getNickname()).isEqualTo("테스터");
        assertThat(found.get().getOnboardingCompleted()).isFalse();
    }

    @DisplayName("온보딩 완료 후 onboardingCompleted 가 true 로 변경된다.")
    @Test
    void completeOnboarding() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        // when
        user.completeOnboarding();

        // then
        assertThat(user.getOnboardingCompleted()).isTrue();
    }

    @DisplayName("프로필을 업데이트한다.")
    @Test
    void updateProfile() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        // when
        user.updateProfile("새닉네임", "https://new-image.url");

        // then
        assertThat(user.getNickname()).isEqualTo("새닉네임");
        assertThat(user.getProfileImageUrl()).isEqualTo("https://new-image.url");
    }

    @DisplayName("User 를 삭제한다.")
    @Test
    void delete() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        // when
        userRepository.deleteById(user.getId());

        // then
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @DisplayName("중복된 이메일로 User 를 저장하면 예외가 발생한다.")
    @Test
    void duplicateEmailThrowsException() {
        // given
        userRepository.saveAndFlush(User.builder()
                .email("duplicate@test.com")
                .nickname("테스터1")
                .build());

        // when & then
        assertThatThrownBy(() ->
                userRepository.saveAndFlush(User.builder()
                        .email("duplicate@test.com")
                        .nickname("테스터2")
                        .build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
