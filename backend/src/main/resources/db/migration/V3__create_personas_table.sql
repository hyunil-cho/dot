-- V3__create_personas_table.sql
-- Persona (전화번호부 항목) 테이블 생성

CREATE TABLE personas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(500) NOT NULL COMMENT '암호화된 이름',
    phone_number VARCHAR(500) NOT NULL COMMENT '암호화된 전화번호',
    relationship VARCHAR(100) DEFAULT NULL COMMENT '관계 (예: 어머니, 친구)',
    profile_image_url VARCHAR(1000) DEFAULT NULL COMMENT 'S3 프로필 이미지 URL',
    memo TEXT DEFAULT NULL COMMENT 'AI 참조용 메모',
    learning_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT '학습 상태',
    last_training_job_id VARCHAR(100) DEFAULT NULL COMMENT 'AI Engine Job ID',
    last_training_updated_at DATETIME(6) DEFAULT NULL COMMENT '마지막 학습 상태 동기화 시간',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Soft Delete 플래그',
    deleted_at DATETIME(6) DEFAULT NULL COMMENT '삭제 일시',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_personas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_personas_user_phone UNIQUE (user_id, phone_number),
    INDEX idx_persona_user_deleted (user_id, is_deleted),
    INDEX idx_persona_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Persona (전화번호부) 테이블';


