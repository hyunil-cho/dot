package com.dot.backend.domain.call;

import com.dot.backend.domain.common.BaseEntity;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "call_logs",
    indexes = {
        @Index(name = "idx_call_log_user_started", columnList = "user_id, started_at DESC")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CallLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona; // ON DELETE CASCADE (Persona 삭제 시 함께 삭제)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_session_id")
    private CallSession callSession;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    public static CallLog fromSession(CallSession session) {
        return CallLog.builder()
                .user(session.getUser())
                .persona(session.getPersona())
                .callSession(session)
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .build();
    }
}

