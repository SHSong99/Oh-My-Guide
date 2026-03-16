package com.e103.ohmyguide.domain.chatmessage.repository;

import com.e103.ohmyguide.IntegrationTestSupport;
import com.e103.ohmyguide.domain.chatmessage.entity.ChatMessage;
import com.e103.ohmyguide.domain.chatmessage.entity.ChatRole;
import com.e103.ohmyguide.domain.user.entity.User;
import com.e103.ohmyguide.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("로그인 사용자의 ChatMessage 를 저장하고 조회한다.")
    @Test
    void saveAndFindWithUser() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        ChatMessage message = ChatMessage.builder()
                .user(user)
                .role(ChatRole.USER)
                .content("서울 근처 관광지 추천해줘")
                .build();

        // when
        ChatMessage saved = chatMessageRepository.save(message);
        Optional<ChatMessage> found = chatMessageRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("서울 근처 관광지 추천해줘");
        assertThat(found.get().getRole()).isEqualTo(ChatRole.USER);
        assertThat(found.get().getUser().getEmail()).isEqualTo("test@test.com");
    }

    @DisplayName("비로그인 사용자의 ChatMessage 는 user 가 null 이다.")
    @Test
    void saveAndFindWithoutUser() {
        // given
        ChatMessage message = ChatMessage.builder()
                .user(null)
                .role(ChatRole.ASSISTANT)
                .content("경복궁을 추천합니다.")
                .build();

        // when
        ChatMessage saved = chatMessageRepository.save(message);
        Optional<ChatMessage> found = chatMessageRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isNull();
        assertThat(found.get().getRole()).isEqualTo(ChatRole.ASSISTANT);
    }

    @DisplayName("한 사용자의 대화 메시지 목록을 저장하고 전체 조회한다.")
    @Test
    void saveConversationAndFindAll() {
        // given
        User user = userRepository.save(User.builder()
                .email("test@test.com")
                .nickname("테스터")
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .user(user).role(ChatRole.USER).content("서울 관광지 추천해줘").build());
        chatMessageRepository.save(ChatMessage.builder()
                .user(user).role(ChatRole.ASSISTANT).content("경복궁을 추천합니다.").build());
        chatMessageRepository.save(ChatMessage.builder()
                .user(user).role(ChatRole.USER).content("다른 곳도 알려줘").build());

        // when
        List<ChatMessage> all = chatMessageRepository.findAll();

        // then
        assertThat(all).hasSize(3);
        assertThat(all).extracting(ChatMessage::getRole)
                .containsExactly(ChatRole.USER, ChatRole.ASSISTANT, ChatRole.USER);
    }

    @DisplayName("ChatMessage 를 삭제한다.")
    @Test
    void delete() {
        // given
        ChatMessage saved = chatMessageRepository.save(ChatMessage.builder()
                .role(ChatRole.USER)
                .content("서울 관광지 추천해줘")
                .build());

        // when
        chatMessageRepository.deleteById(saved.getId());

        // then
        assertThat(chatMessageRepository.findById(saved.getId())).isEmpty();
    }
}
