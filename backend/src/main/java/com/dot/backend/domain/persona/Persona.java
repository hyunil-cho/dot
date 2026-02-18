package com.dot.backend.domain.persona;

import com.dot.backend.domain.persona.ConversationSample;
import com.dot.backend.domain.common.BaseEntity;
import com.dot.backend.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "personas",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "phone_number"})
    },
    indexes = {
        @Index(name = "idx_persona_user_deleted", columnList = "user_id, is_deleted")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Persona extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String name; // 암호화 저장

    @NotBlank
    @Column(name = "phone_number", nullable = false, length = 500)
    private String phoneNumber; // 암호화 저장

    @Column(length = 100)
    private String relationship; // 예: 어머니, 아버지, 친구 등

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl; // S3 URL

    @Column(columnDefinition = "TEXT")
    private String memo; // AI 참조용 메모

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_status", nullable = false, length = 20)
    @Builder.Default
    private LearningStatus learningStatus = LearningStatus.NOT_STARTED;

    @Column(name = "last_training_job_id", length = 100)
    private String lastTrainingJobId; // AI Engine에서 반환한 Job ID

    @Column(name = "persona_model_id", length = 100) // Renamed field
    private String personaModelId; // 학습 완료된 페르소나 모델 ID

    @Column(name = "last_training_updated_at")
    private LocalDateTime lastTrainingUpdatedAt; // 마지막 학습 상태 동기화 시간

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // New relationships
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Initialize with empty list
    private List<PersonaTrait> traits = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Initialize with empty list
    private List<ConversationSample> samples = new ArrayList<>();


    // 비즈니스 로직 - existing methods, with modifications if needed

    public void updateProfile(String name, String relationship, String memo) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        this.relationship = relationship;
        this.memo = memo;
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
    }

    public void updateLearningStatus(LearningStatus status) {
        this.learningStatus = status;
        this.lastTrainingUpdatedAt = LocalDateTime.now();
    }

    public void updateLastTrainingJobId(String jobId) {
        this.lastTrainingJobId = jobId;
        this.lastTrainingUpdatedAt = LocalDateTime.now();
    }
    
    public void completeTraining(String personaModelId) {
        this.learningStatus = LearningStatus.COMPLETED;
        this.personaModelId = personaModelId;
        this.lastTrainingUpdatedAt = LocalDateTime.now();
    }


    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        if (!this.isDeleted) {
            throw new IllegalStateException("Persona가 삭제되지 않은 상태입니다");
        }
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public boolean canStartLearning() {
        return !this.isDeleted &&
               (this.learningStatus == LearningStatus.NOT_STARTED ||
                this.learningStatus == LearningStatus.FAILED);
    }

    public boolean isLearningCompleted() {
        return this.learningStatus == LearningStatus.COMPLETED;
    }

    public boolean belongsTo(User user) {
        return this.user.getId().equals(user.getId());
    }
}


