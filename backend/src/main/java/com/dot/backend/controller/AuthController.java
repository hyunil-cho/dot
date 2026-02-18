package com.dot.backend.controller;

import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.auth.*;
import com.dot.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 *
 * - POST /api/auth/signup - 회원가입
 * - POST /api/auth/login - 로그인
 * - POST /api/auth/refresh - Access Token 갱신
 * - POST /api/auth/logout - 로그아웃
 * - DELETE /api/auth/withdraw - 회원탈퇴
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API - 회원가입, 로그인, 토큰 관리, 회원탈퇴")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "이메일과 비밀번호로 새로운 계정을 생성합니다.\n\n" +
            "**비밀번호 요구사항:**\n" +
            "- 최소 8자 이상\n" +
            "- 영문, 숫자, 특수문자(@$!%*#?&) 각 1개 이상 포함\n\n" +
            "**예시:** `Test1234!`"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "회원가입 성공"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (이메일 형식 오류, 비밀번호 규칙 위반)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"올바른 이메일 형식이 아닙니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 이메일",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"이미 가입된 이메일입니다\"}"
                )
            )
        )
    })
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.\n\n" +
            "**발급되는 토큰:**\n" +
            "- **Access Token**: API 요청 시 사용 (유효기간: 15분)\n" +
            "- **Refresh Token**: Access Token 갱신 시 사용 (유효기간: 7일)\n\n" +
            "**사용 방법:**\n" +
            "1. 로그인 성공 시 `accessToken`과 `refreshToken`을 저장\n" +
            "2. API 요청 시 헤더에 `Authorization: Bearer {accessToken}` 포함\n" +
            "3. Access Token 만료 시 `/api/auth/refresh`로 갱신"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n" +
                        "  \"refreshToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n" +
                        "  \"tokenType\": \"Bearer\",\n" +
                        "  \"userId\": 1,\n" +
                        "  \"email\": \"user@example.com\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (이메일 또는 비밀번호 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"이메일 또는 비밀번호가 일치하지 않습니다\"}"
                )
            )
        )
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Access Token 갱신
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Access Token 갱신",
        description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.\n\n" +
            "**사용 시나리오:**\n" +
            "- Access Token이 만료되었을 때 (401 Unauthorized)\n" +
            "- 앱 재시작 시 저장된 Refresh Token으로 자동 로그인\n\n" +
            "**주의사항:**\n" +
            "- Refresh Token도 만료된 경우 재로그인 필요\n" +
            "- 새로운 Access Token만 발급되며, Refresh Token은 재발급되지 않음"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "토큰 갱신 성공",
            content = @Content(
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"accessToken\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\n" +
                        "  \"tokenType\": \"Bearer\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "유효하지 않거나 만료된 Refresh Token",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"유효하지 않은 Refresh Token입니다\"}"
                )
            )
        )
    })
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "Refresh Token을 무효화하여 로그아웃합니다.\n\n" +
            "**처리 과정:**\n" +
            "1. 서버에서 해당 Refresh Token을 DB에서 삭제\n" +
            "2. 클라이언트는 저장된 Access Token과 Refresh Token을 제거\n\n" +
            "**인증 필요:**\n" +
            "- Authorization 헤더에 Bearer Token 필요"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"인증이 필요합니다\"}"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    /**
     * 회원 탈퇴 (Hard Delete)
     *
     * FR-01-03: 사용자의 요청 시 계정과 관련된 모든 데이터(학습 모델 포함)를 파기한다.
     */
    @DeleteMapping("/withdraw")
    @Operation(
        summary = "회원 탈퇴",
        description = "사용자 계정 및 관련된 모든 데이터를 영구 삭제합니다.\n\n" +
            "**삭제되는 데이터:**\n" +
            "- 사용자 계정 정보\n" +
            "- 사용자가 생성한 모든 Persona\n" +
            "- 채팅 기록 및 세션\n" +
            "- 학습 데이터 및 AI 모델 (TODO: AI Engine 연동 필요)\n" +
            "- S3에 저장된 파일들\n\n" +
            "**주의사항:**\n" +
            "- 탈퇴 시 모든 데이터가 **영구 삭제**되며 복구 불가능\n" +
            "- 본인 확인을 위해 비밀번호 입력 필수\n" +
            "- 탈퇴 사유는 선택사항 (최대 500자)\n\n" +
            "**인증 필요:**\n" +
            "- Authorization 헤더에 Bearer Token 필요"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "회원 탈퇴 성공 (No Content)"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (비밀번호 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"비밀번호가 일치하지 않습니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"인증이 필요합니다\"}"
                )
            )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> withdraw(
        @Valid @RequestBody WithdrawRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // JWT에서 추출된 사용자 정보로 User 조회
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        authService.withdraw(currentUser, request);

        return ResponseEntity.noContent().build();  // 204 No Content
    }

    /**
     * Refresh Token 요청 DTO
     */
    public record RefreshTokenRequest(
        @Schema(description = "Refresh token value", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken
    ) {}
}
