package com.dot.backend.dto.persona;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persona 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Persona 수정 요청")
public class PersonaUpdateRequest {

    @Schema(
        description = "Persona 이름 (변경하지 않으려면 null)",
        example = "엄마",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 100, message = "이름은 100자 이내로 입력해주세요")
    private String name;

    @Schema(
        description = "관계 (변경하지 않으려면 null)",
        example = "어머니",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 100, message = "관계는 100자 이내로 입력해주세요")
    private String relationship;

    @Schema(
        description = "메모 (변경하지 않으려면 null)",
        example = "항상 따뜻하고 다정한 말투를 사용하세요.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 5000, message = "메모는 5000자 이내로 입력해주세요")
    private String memo;

    @Schema(
        description = "프로필 이미지 URL (변경하지 않으려면 null)",
        example = "https://dot-bucket.s3.amazonaws.com/profiles/123.jpg",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String profileImageUrl;
}

