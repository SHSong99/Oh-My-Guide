package com.e103.ohmyguide.domain.chatmessage.repository;

import com.e103.ohmyguide.domain.chatmessage.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
