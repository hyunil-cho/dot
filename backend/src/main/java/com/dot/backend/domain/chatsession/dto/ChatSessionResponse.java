package com.dot.backend.domain.chatsession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ChatSessionResponse {
    private Long sessionId;
    private Long personaId;
    private String personaName;
    private String status;
    private LocalDateTime startedAt;
}

