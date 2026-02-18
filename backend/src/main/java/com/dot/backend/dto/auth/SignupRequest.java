package com.dot.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원가입 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원가입 요청")
public class SignupRequest {

    @Schema(
        description = "이메일 주소 (로그인 ID로 사용)",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Schema(
        description = "비밀번호 (최소 8자, 영문+숫자+특수문자 포함)",
        example = "Test1234!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        pattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$"
    )
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다"
    )
    private String password;

    @Schema(
        description = "사용자 이름 (선택사항)",
        example = "홍길동",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String name; // 선택사항
}

