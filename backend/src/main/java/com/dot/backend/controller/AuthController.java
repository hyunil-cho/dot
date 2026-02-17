package com.dot.backend.controller;

import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.auth.*;
import com.dot.backend.service.AuthService;
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
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Access Token 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
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
    public record RefreshTokenRequest(String refreshToken) {}
}




