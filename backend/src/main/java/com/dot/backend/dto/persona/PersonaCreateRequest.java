package com.dot.backend.dto.persona;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persona 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Persona 생성 요청")
public class PersonaCreateRequest {

    @Schema(
        description = "Persona 이름 (연락처에 표시될 이름)",
        example = "엄마",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이름을 입력해주세요")
    @Size(max = 100, message = "이름은 100자 이내로 입력해주세요")
    private String name;

    @Schema(
        description = "전화번호 (고유 식별자로 사용)",
        example = "010-1234-5678",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "전화번호를 입력해주세요")
    @Pattern(
        regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
        message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)"
    )
    private String phoneNumber;

    @Schema(
        description = "관계 (예: 어머니, 아버지, 친구 등)",
        example = "어머니",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 100, message = "관계는 100자 이내로 입력해주세요")
    private String relationship;

    @Schema(
        description = "메모 (AI 참조용, 성향이나 특징 등을 자유롭게 작성)",
        example = "항상 따뜻하고 다정한 말투를 사용하세요. 걱정이 많으시고 건강을 챙겨주시는 편입니다.",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    @Size(max = 5000, message = "메모는 5000자 이내로 입력해주세요")
    private String memo;

    @Schema(
        description = "프로필 이미지 URL (S3 업로드 후 받은 URL)",
        example = "https://dot-bucket.s3.amazonaws.com/profiles/123.jpg",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String profileImageUrl;
}

