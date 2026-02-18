-- V10__alter_personas_phone_number_optional.sql
-- 전화번호를 선택 필드로 변경 (더 이상 식별자가 아님)

-- 1. unique constraint 제거
ALTER TABLE personas DROP INDEX uq_personas_user_phone;

-- 2. phone_number를 nullable로 변경
ALTER TABLE personas MODIFY COLUMN phone_number VARCHAR(500) NULL COMMENT '암호화된 전화번호 (선택 사항)';

