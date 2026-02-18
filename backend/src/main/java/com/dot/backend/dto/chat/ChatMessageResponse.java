package com.dot.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "채팅 메시지 응답")
public class ChatMessageResponse {

    @Schema(description = "메시지 ID", example = "123")
    private Long messageId;

    @Schema(description = "메시지 내용", example = "안녕하세요!")
    private String content;

    @Schema(description = "역할 (USER/ASSISTANT)", example = "ASSISTANT")
    private String role;

    @Schema(description = "사용자 메시지 여부", example = "false")
    private Boolean isFromUser;

    @Schema(description = "생성 시간", example = "2026-02-18T10:30:00")
    private LocalDateTime createdAt;
}
