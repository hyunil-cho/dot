-- V1__create_users_table.sql
-- 사용자(회원) 테이블 생성

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(500) NOT NULL UNIQUE COMMENT '암호화된 이메일',
    password VARCHAR(500) NOT NULL COMMENT 'BCrypt 해싱된 비밀번호',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 테이블';


