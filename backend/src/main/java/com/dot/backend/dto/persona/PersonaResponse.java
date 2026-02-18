package com.dot.backend.dto.persona;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persona 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Persona 정보")
public class PersonaResponse {

    @Schema(description = "Persona ID", example = "1")
    private Long id;

    @Schema(description = "이름", example = "엄마")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;

    @Schema(description = "관계", example = "어머니")
    private String relationship;

    @Schema(description = "프로필 이미지 URL", example = "https://dot-bucket.s3.amazonaws.com/profiles/123.jpg")
    private String profileImageUrl;

    @Schema(description = "메모 (AI 참조용)", example = "따뜻하고 다정한 말투")
    private String memo;

    @Schema(description = "생성일시", example = "2026-02-18T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2026-02-18T15:45:00")
    private LocalDateTime updatedAt;
}
