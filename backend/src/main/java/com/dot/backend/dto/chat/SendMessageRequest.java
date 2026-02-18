package com.dot.backend.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 메시지 전송 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 전송 요청")
public class SendMessageRequest {

    @Schema(
        description = "메시지 내용",
        example = "안녕하세요! 오늘 날씨가 좋네요.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "메시지 내용을 입력해주세요")
    @Size(max = 5000, message = "메시지는 5000자 이내로 입력해주세요")
    private String content;
}

