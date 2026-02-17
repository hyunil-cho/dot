package com.dot.backend.service;

import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.token.RefreshToken;
import com.dot.backend.domain.token.repository.RefreshTokenRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.auth.*;
import com.dot.backend.exception.DuplicateEmailException;
import com.dot.backend.exception.InvalidCredentialsException;
import com.dot.backend.exception.InvalidRefreshTokenException;
import com.dot.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증 서비스
 *
 * - 회원가입
 * - 로그인 (Access Token + Refresh Token 발급)
 * - Access Token 갱신
 * - 로그아웃
 * - 회원탈퇴
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("User signed up: {}", request.getEmail());
    }

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        // Access Token 생성 (15분)
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        // Refresh Token 생성 (7일)
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);

        // Refresh Token 저장
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(refreshTokenExpiresAt)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.of(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail()
        );
    }

    /**
     * Access Token 갱신
     */
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        // Refresh Token 검증 (JWT)
        Long userId = jwtTokenProvider.validateRefreshToken(refreshToken);

        // DB에서 Refresh Token 조회
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        // 유효성 검증
        if (!tokenEntity.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        // 사용자 확인
        if (!tokenEntity.getUser().getId().equals(userId)) {
            throw new InvalidRefreshTokenException();
        }

        // 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);

        log.info("Access token refreshed for user: {}", userId);

        return TokenResponse.of(newAccessToken);
    }

    /**
     * 로그아웃 (Refresh Token 폐기)
     */
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElse(null);

        if (tokenEntity != null) {
            tokenEntity.revoke();
            log.info("User logged out: {}", tokenEntity.getUser().getEmail());
        }
    }

    /**
     * 모든 디바이스에서 로그아웃
     */
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All tokens revoked for user: {}", userId);
    }

    /**
     * 회원 탈퇴 (Hard Delete)
     *
     * FR-01-03: 사용자의 요청 시 계정과 관련된 모든 데이터(학습 모델 포함)를 파기한다.
     */
    @Transactional
    public void withdraw(User currentUser, WithdrawRequest request) {
        // 1. ✅ 비밀번호 재확인
        if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
            throw new InvalidCredentialsException("비밀번호가 일치하지 않습니다");
        }

        // 2. TODO: AI 모델 삭제 요청
        // ⚠️ AI Engine API 확인 후 구현 필요
        // - AI Engine에 학습 모델 삭제 API가 존재하는지 확인
        // - 존재하는 경우: aiEngineClient.deletePersonaModels(currentUser.getId());
        // - API 스펙: 엔드포인트, 파라미터, 응답 형식 확인 필요
        // - 실패 시 롤백 전략 수립 필요 (일관성 vs 가용성)

        // 3. ✅ Persona 삭제 (VoiceData, CallSession, CallLog CASCADE 삭제)
        personaRepository.deleteAllByUserId(currentUser.getId());

        // 4. ✅ Refresh Token 삭제
        refreshTokenRepository.deleteAllByUserId(currentUser.getId());

        // 5. ✅ User Hard Delete
        userRepository.delete(currentUser);

        // 6. ✅ 탈퇴 사유 로깅 (통계/분석용)
        if (request.getReason() != null && !request.getReason().isBlank()) {
            log.info("User withdrawal - userId: {}, email: {}, reason: {}",
                currentUser.getId(), currentUser.getEmail(), request.getReason());
        } else {
            log.info("User withdrawal - userId: {}, email: {}",
                currentUser.getId(), currentUser.getEmail());
        }
    }
}



