package com.dot.backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 로그인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 성공 응답")
public class LoginResponse {

    @Schema(
        description = "JWT Access Token (API 요청 시 사용, 유효기간: 15분)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
    )
    private String accessToken;

    @Schema(
        description = "JWT Refresh Token (Access Token 갱신 시 사용, 유효기간: 7일)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjE2MjM5MDIyfQ.abc123def456ghi789jkl012mno345pqr678stu901vwx234yz"
    )
    private String refreshToken;

    @Schema(
        description = "토큰 타입 (항상 Bearer)",
        example = "Bearer",
        defaultValue = "Bearer"
    )
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(
        description = "사용자 고유 ID",
        example = "1"
    )
    private Long userId;

    @Schema(
        description = "사용자 이메일",
        example = "user@example.com"
    )
    private String email;

    public static LoginResponse of(String accessToken, String refreshToken, Long userId, String email) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(userId)
                .email(email)
                .build();
    }
}


