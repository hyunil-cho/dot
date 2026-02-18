package com.dot.backend.domain.chatsession.repository;

import com.dot.backend.domain.chatsession.ChatLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {

    // 최근 채팅 목록 조회 (페이징 지원)
    @Query("SELECT c FROM ChatLog c WHERE c.user.id = :userId ORDER BY c.startedAt DESC")
    List<ChatLog> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Persona별 채팅 기록 조회
    @Query("SELECT c FROM ChatLog c WHERE c.persona.id = :personaId ORDER BY c.startedAt DESC")
    List<ChatLog> findByPersonaIdOrderByStartedAtDesc(@Param("personaId") Long personaId);

    // User의 총 채팅 횟수
    long countByUserId(Long userId);

    // Persona의 총 채팅 횟수
    long countByPersonaId(Long personaId);
}


