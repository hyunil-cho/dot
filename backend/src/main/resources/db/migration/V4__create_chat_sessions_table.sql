-- V4__create_chat_sessions_table.sql
-- 채팅 세션 테이블 생성

CREATE TABLE chat_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INIT' COMMENT '채팅 상태: INIT, ACTIVE, ENDED',
    s3_key VARCHAR(500) DEFAULT NULL,
    original_file_name VARCHAR(255) DEFAULT NULL,
    selected_speaker VARCHAR(100) DEFAULT NULL,
    system_prompt TEXT DEFAULT NULL,
    started_at DATETIME(6) DEFAULT NULL COMMENT '채팅 시작 시간',
    ended_at DATETIME(6) DEFAULT NULL COMMENT '채팅 종료 시간',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_chat_sessions_persona FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_chat_session_persona_status (persona_id, status),
    INDEX idx_chat_session_user (user_id),
    INDEX idx_chat_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='채팅 세션 테이블';


