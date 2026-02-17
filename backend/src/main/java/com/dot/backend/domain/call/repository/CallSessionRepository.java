package com.dot.backend.domain.call.repository;

import com.dot.backend.domain.call.CallSession;
import com.dot.backend.domain.call.CallSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CallSessionRepository extends JpaRepository<CallSession, Long> {

    // Persona의 활성 세션 조회 (Domain Invariant: Only one ACTIVE CallSession per Persona)
    @Query("SELECT c FROM CallSession c WHERE c.persona.id = :personaId AND c.status = 'ACTIVE'")
    Optional<CallSession> findActiveSessionByPersonaId(@Param("personaId") Long personaId);

    // User의 모든 세션 조회
    List<CallSession> findByUserIdOrderByStartedAtDesc(Long userId);

    // Persona의 모든 세션 조회
    List<CallSession> findByPersonaIdOrderByStartedAtDesc(Long personaId);

    // 특정 상태의 세션 조회
    @Query("SELECT c FROM CallSession c WHERE c.status = :status")
    List<CallSession> findByStatus(@Param("status") CallSessionStatus status);

    // ID와 User로 조회 (권한 검증용)
    @Query("SELECT c FROM CallSession c WHERE c.id = :id AND c.user.id = :userId")
    Optional<CallSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

