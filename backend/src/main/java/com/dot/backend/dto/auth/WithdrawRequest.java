package com.dot.backend.dto.auth;

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
public class WithdrawRequest {

    @NotBlank(message = "비밀번호를 입력해주세요")
    private String password;

    @Size(max = 500, message = "탈퇴 사유는 500자 이내로 입력해주세요")
    private String reason;  // 선택 항목
}

