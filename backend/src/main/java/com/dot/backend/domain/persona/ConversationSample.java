package com.dot.backend.domain.persona;

import com.dot.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "conversation_sample")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationSample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Lob
    @Column(nullable = false)
    private String message;

    public enum Role {
        USER, PERSONA
    }

    @Builder
    public ConversationSample(Persona persona, Role role, String message) {
        this.persona = persona;
        this.role = role;
        this.message = message;
    }
}
