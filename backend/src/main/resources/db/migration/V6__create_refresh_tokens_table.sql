-- V7__create_refresh_tokens_table.sql
-- Refresh Token 테이블 생성

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE COMMENT 'Refresh Token 문자열',
    device_id VARCHAR(255) DEFAULT NULL COMMENT '디바이스 식별자 (선택)',
    expires_at DATETIME(6) NOT NULL COMMENT '만료 시간',
    revoked BOOLEAN NOT NULL DEFAULT FALSE COMMENT '폐기 여부',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_token (token),
    INDEX idx_refresh_token_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Refresh Token 관리 테이블';

