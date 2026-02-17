-- V4__create_voice_data_table.sql
-- 음성 데이터 테이블 생성

CREATE TABLE voice_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    file_url VARCHAR(1000) NOT NULL COMMENT '암호화된 S3 URL',
    file_name VARCHAR(500) NOT NULL COMMENT '암호화된 파일명',
    file_size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    ai_file_id VARCHAR(100) DEFAULT NULL COMMENT 'AI Engine에 업로드된 파일 ID',
    uploaded_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_voice_data_persona FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    INDEX idx_voice_data_persona (persona_id),
    INDEX idx_voice_data_uploaded (uploaded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='음성 파일 메타데이터 테이블';


