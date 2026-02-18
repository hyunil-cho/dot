package com.dot.backend.domain.chat.repository;

import com.dot.backend.domain.chat.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByUserIdAndPersonaIdOrderByCreatedAtAsc(Long userId, Long personaId);

    Optional<ChatMessage> findFirstByPersonaIdAndUserIdOrderByCreatedAtDesc(Long personaId, Long userId);

}
