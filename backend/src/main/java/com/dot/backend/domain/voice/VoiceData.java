package com.dot.backend.domain.voice;

import com.dot.backend.domain.common.BaseEntity;
import com.dot.backend.domain.persona.Persona;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "voice_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VoiceData extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @NotBlank
    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl; // S3 URL (암호화)

    @NotBlank
    @Column(name = "file_name", nullable = false, length = 500)
    private String fileName; // 암호화

    @Positive
    @Column(name = "file_size", nullable = false)
    private Long fileSize; // bytes

    @Column(name = "ai_file_id", length = 100)
    private String aiFileId; // AI Engine에 업로드된 파일 ID

    @Column(name = "uploaded_at", nullable = false)
    private java.time.LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = java.time.LocalDateTime.now();
        }
    }
}


