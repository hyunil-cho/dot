-- V7__create_call_logs_table.sql
-- 통화 기록 테이블 생성

CREATE TABLE call_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    persona_id BIGINT NOT NULL,
    call_session_id BIGINT DEFAULT NULL,
    started_at DATETIME(6) NOT NULL COMMENT '통화 시작 시간',
    ended_at DATETIME(6) DEFAULT NULL COMMENT '통화 종료 시간',
    duration_seconds INT DEFAULT NULL COMMENT '통화 시간 (초)',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_call_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_call_logs_persona FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    CONSTRAINT fk_call_logs_session FOREIGN KEY (call_session_id) REFERENCES call_sessions(id) ON DELETE SET NULL,
    INDEX idx_call_log_user_started (user_id, started_at DESC),
    INDEX idx_call_log_persona (persona_id),
    INDEX idx_call_log_session (call_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='통화 기록 테이블 (최근 통화 목록용)';

