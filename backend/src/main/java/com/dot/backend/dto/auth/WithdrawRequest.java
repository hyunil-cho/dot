package com.dot.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 탈퇴 요청")
public class WithdrawRequest {

    @Schema(
        description = "본인 확인을 위한 현재 비밀번호",
        example = "Test1234!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @Schema(
        description = "탈퇴 사유 (선택사항, 최대 500자)",
        example = "서비스 이용이 불편해서",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 500
    )
    @Size(max = 500, message = "탈퇴 사유는 500자 이내로 입력해주세요")
    private String reason;  // 선택 항목
}

