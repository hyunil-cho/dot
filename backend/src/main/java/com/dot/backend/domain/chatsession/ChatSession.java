package com.dot.backend.domain.chatsession;

import com.dot.backend.domain.common.BaseEntity;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "call_sessions",
    indexes = {
        @Index(name = "idx_call_session_persona_status", columnList = "persona_id, status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ChatSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChatSessionStatus status = ChatSessionStatus.INIT;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 상태 전이 메서드 (State Machine Rules 준수)

    public void start() {
        if (this.status != ChatSessionStatus.INIT) {
            throw new IllegalStateException("Can only start from INIT state");
        }
        this.status = ChatSessionStatus.ACTIVE;
        this.startedAt = LocalDateTime.now();
    }

    public void end() {
        if (this.status != ChatSessionStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 채팅을 종료할 수 있습니다");
        }
        this.status = ChatSessionStatus.ENDED;
        this.endedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == ChatSessionStatus.ACTIVE;
    }

    public boolean isEnded() {
        return this.status == ChatSessionStatus.ENDED;
    }

    public boolean belongsTo(User user) {
        return this.user.getId().equals(user.getId());
    }

    /**
     * 채팅 시간을 동적으로 계산
     * @return 채팅 시간 (초), 시작/종료 시간이 없으면 null
     */
    public Integer getDurationSeconds() {
        if (this.startedAt == null || this.endedAt == null) {
            return null;
        }
        return (int) Duration.between(this.startedAt, this.endedAt).getSeconds();
    }
}




