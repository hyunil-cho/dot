package com.dot.backend.domain.persona.repository;

import com.dot.backend.domain.persona.ConversationSample;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSampleRepository extends JpaRepository<ConversationSample, Long> {
}
