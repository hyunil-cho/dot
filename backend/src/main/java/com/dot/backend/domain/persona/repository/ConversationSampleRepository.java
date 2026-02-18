package com.dot.backend.domain.persona.repository;

import com.dot.backend.domain.persona.ConversationSample;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationSampleRepository extends JpaRepository<ConversationSample, Long> {

    /**
     * Persona의 대화 샘플 조회 (최근 순으로 정렬)
     */
    List<ConversationSample> findByPersonaIdOrderByCreatedAtDesc(Long personaId, Pageable pageable);

    /**
     * Persona의 모든 대화 샘플 조회
     */
    List<ConversationSample> findByPersonaId(Long personaId);
}
