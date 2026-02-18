package com.dot.backend.domain.persona;

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

    @Column(name = "phone_number", length = 500)
    private String phoneNumber; // 암호화 저장 (선택 사항)

    @Column(length = 100)
    private String relationship; // 예: 어머니, 아버지, 친구 등

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl; // S3 URL

    @Column(columnDefinition = "TEXT")
    private String memo; // AI 참조용 메모 (시스템 프롬프트 생성에 사용)

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PersonaTrait> traits = new ArrayList<>();

    @OneToMany(mappedBy = "persona", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ConversationSample> samples = new ArrayList<>();


    // 비즈니스 로직

    public void updateProfile(String name, String phoneNumber, String relationship, String memo) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
        if (relationship != null) {
            this.relationship = relationship;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }

    public void updateProfileImage(String imageUrl) {
        this.profileImageUrl = imageUrl;
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

    public boolean belongsTo(User user) {
        return this.user.getId().equals(user.getId());
    }

    /**
     * Persona가 채팅 가능한 상태인지 확인
     * - 삭제되지 않은 상태
     * - 최소 1개 이상의 ConversationSample 또는 memo가 있는 경우
     */
    public boolean isReadyForChat() {
        return !this.isDeleted &&
               (!samples.isEmpty() || (memo != null && !memo.isBlank()));
    }
}

