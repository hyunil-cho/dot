package com.dot.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 요청")
public class LoginRequest {

    @Schema(
        description = "가입 시 사용한 이메일 주소",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(
        description = "비밀번호",
        example = "Test1234!",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;
}

