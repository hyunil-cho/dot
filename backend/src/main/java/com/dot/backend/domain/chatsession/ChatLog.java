package com.dot.backend.domain.chatsession;

import com.dot.backend.domain.common.BaseEntity;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "chat_logs",
    indexes = {
        @Index(name = "idx_chat_log_user_started", columnList = "user_id, started_at DESC")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatLog extends BaseEntity {

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
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    public static ChatLog fromSession(ChatSession session) {
        return ChatLog.builder()
                .user(session.getUser())
                .persona(session.getPersona())
                .chatSession(session)
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
                .durationSeconds(session.getDurationSeconds())
                .build();
    }
}

