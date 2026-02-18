package com.dot.backend.dto.persona;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persona 목록 응답 DTO (간소화된 정보)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Persona 목록 항목")
public class PersonaListResponse {

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
}

