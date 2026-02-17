package com.dot.backend.domain.call.repository;

import com.dot.backend.domain.call.CallLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallLogRepository extends JpaRepository<CallLog, Long> {

    // 최근 통화 목록 조회 (페이징 지원)
    @Query("SELECT c FROM CallLog c WHERE c.user.id = :userId ORDER BY c.startedAt DESC")
    List<CallLog> findByUserIdOrderByStartedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // Persona별 통화 기록 조회
    @Query("SELECT c FROM CallLog c WHERE c.persona.id = :personaId ORDER BY c.startedAt DESC")
    List<CallLog> findByPersonaIdOrderByStartedAtDesc(@Param("personaId") Long personaId);

    // User의 총 통화 횟수
    long countByUserId(Long userId);

    // Persona의 총 통화 횟수
    long countByPersonaId(Long personaId);
}

