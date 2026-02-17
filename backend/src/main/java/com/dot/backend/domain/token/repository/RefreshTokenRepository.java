package com.dot.backend.domain.token.repository;

import com.dot.backend.domain.token.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID로 모든 토큰 조회
     */
    List<RefreshToken> findByUserId(Long userId);

    /**
     * 사용자의 유효한 토큰 조회
     */
    @Query("""
        SELECT rt FROM RefreshToken rt
        WHERE rt.user.id = :userId
        AND rt.revoked = false
        AND rt.expiresAt > :now
    """)
    List<RefreshToken> findValidTokensByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );

    /**
     * 사용자의 모든 토큰 폐기 (로그아웃)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllByUserId(@Param("userId") Long userId);

    /**
     * 만료된 토큰 삭제 (배치 작업용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :threshold")
    void deleteExpiredTokens(@Param("threshold") LocalDateTime threshold);

    /**
     * 토큰 존재 여부 확인
     */
    boolean existsByToken(String token);

    /**
     * 사용자의 모든 토큰 삭제 (회원 탈퇴용)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}


