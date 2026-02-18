package com.dot.backend.domain.persona;

import com.dot.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persona_trait")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonaTrait extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @Enumerated(EnumType.STRING)
    @Column(name = "trait_type", nullable = false)
    private TraitType traitType;

    @Lob
    @Column(name = "trait_value", nullable = false)
    private String traitValue;

    public enum TraitType {
        SPEECH_PATTERN, // 말투
        HABIT_WORD,     // 습관어
        PERSONALITY     // 성격
    }

    @Builder
    public PersonaTrait(Persona persona, TraitType traitType, String traitValue) {
        this.persona = persona;
        this.traitType = traitType;
        this.traitValue = traitValue;
    }
}
