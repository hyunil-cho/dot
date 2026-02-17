-- V2__create_user_settings_table.sql
-- 사용자 설정 테이블 생성

CREATE TABLE user_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    call_timeout_seconds INT NOT NULL DEFAULT 300 COMMENT '통화 타임아웃 (초)',
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '알림 수신 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_settings_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 설정 테이블';

