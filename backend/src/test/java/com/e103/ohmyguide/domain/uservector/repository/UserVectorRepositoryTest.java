package com.e103.ohmyguide.domain.uservector.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import com.e103.ohmyguide.domain.uservector.entity.UserVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserVectorRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private UserVectorRepository userVectorRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("UserVector 를 저장하고 ID 로 조회한다.")
    @Test
    void saveAndFindById() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        UserVector userVector = UserVector.builder()
                .user(user)
                .preferenceVector("[0.1, 0.5, 0.3]")
                .build();

        // when
        UserVector saved = userVectorRepository.save(userVector);
        Optional<UserVector> found = userVectorRepository.findById(saved.getUserId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getPreferenceVector()).isEqualTo("[0.1, 0.5, 0.3]");
    }

    @DisplayName("UserVector 의 PK 는 User 의 ID 와 동일하다. (1:1 관계 검증)")
    @Test
    void userVectorPkEqualsUserId() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        UserVector saved = userVectorRepository.save(UserVector.builder()
                .user(user)
                .preferenceVector("[0.1, 0.5, 0.3]")
                .build());

        // then
        assertThat(saved.getUserId()).isEqualTo(user.getId());
    }

    @DisplayName("UserVector 조회 시 연관된 User 를 함께 확인한다.")
    @Test
    void findWithUserRelation() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        userVectorRepository.save(UserVector.builder()
                .user(user)
                .preferenceVector("[0.1, 0.5, 0.3]")
                .build());

        // when
        UserVector found = userVectorRepository.findById(user.getId()).orElseThrow();

        // then
        assertThat(found.getUser().getEmail()).isEqualTo("test@test.com");
    }

    @DisplayName("preferenceVector 를 업데이트한다.")
    @Test
    void updatePreferenceVector() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        UserVector userVector = userVectorRepository.save(UserVector.builder()
                .user(user)
                .preferenceVector("[0.1, 0.5, 0.3]")
                .build());

        // when
        userVector.updatePreferenceVector("[0.9, 0.1, 0.8]");

        // then
        assertThat(userVector.getPreferenceVector()).isEqualTo("[0.9, 0.1, 0.8]");
    }
}
