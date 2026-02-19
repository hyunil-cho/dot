package com.dot.backend.dto.chat;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatSessionListResponse {
    private Long sessionId;
    private Long personaId;
    private String personaName;
    private String lastMessage;
    private LocalDateTime updatedAt;
}
