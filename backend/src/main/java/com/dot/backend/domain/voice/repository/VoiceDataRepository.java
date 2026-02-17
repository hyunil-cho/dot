package com.dot.backend.domain.voice.repository;

import com.dot.backend.domain.voice.VoiceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceDataRepository extends JpaRepository<VoiceData, Long> {

    List<VoiceData> findByPersonaId(Long personaId);

    @Query("SELECT v FROM VoiceData v WHERE v.persona.id = :personaId ORDER BY v.uploadedAt DESC")
    List<VoiceData> findByPersonaIdOrderByUploadedAtDesc(@Param("personaId") Long personaId);

    long countByPersonaId(Long personaId);

    @Query("SELECT SUM(v.fileSize) FROM VoiceData v WHERE v.persona.id = :personaId")
    Long getTotalFileSizeByPersonaId(@Param("personaId") Long personaId);
}

