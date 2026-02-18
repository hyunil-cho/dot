package com.dot.backend.domain.user;

import com.dot.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "call_timeout_seconds", nullable = false)
    @Builder.Default
    private Integer callTimeoutSeconds = 300; // 기본 5분

    @Column(name = "notification_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEnabled = true;

    public void updateCallTimeout(Integer seconds) {
        if (seconds < 30 || seconds > 3600) {
            throw new IllegalArgumentException("채팅 타임아웃은 30초에서 3600초 사이여야 합니다");
        }
        this.callTimeoutSeconds = seconds;
    }

    public void toggleNotification() {
        this.notificationEnabled = !this.notificationEnabled;
    }
}

