package com.dot.backend.domain.chatsession.repository;

import com.dot.backend.domain.chatsession.ChatSession;
import com.dot.backend.domain.chatsession.ChatSessionStatus;
import com.dot.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // Persona의 활성 세션 조회 (Domain Invariant: Only one ACTIVE ChatSession per Persona)
    @Query("SELECT c FROM ChatSession c WHERE c.persona.id = :personaId AND c.status = 'ACTIVE'")
    Optional<ChatSession> findActiveSessionByPersonaId(@Param("personaId") Long personaId);

    // User의 모든 세션 조회
    List<ChatSession> findByUserIdOrderByStartedAtDesc(Long userId);

    // User의 모든 세션 조회 (업데이트순)
    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);

    // Persona의 모든 세션 조회
    List<ChatSession> findByPersonaIdOrderByStartedAtDesc(Long personaId);

    // 특정 상태의 세션 조회
    @Query("SELECT c FROM ChatSession c WHERE c.status = :status")
    List<ChatSession> findByStatus(@Param("status") ChatSessionStatus status);

    // ID와 User로 조회 (권한 검증용)
    @Query("SELECT c FROM ChatSession c WHERE c.id = :id AND c.user.id = :userId")
    Optional<ChatSession> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}


