-- V11__cleanup_chat_session.sql
-- ChatSession에서 Persona와 중복되는 필드 제거

-- s3_key, selected_speaker 컬럼 제거 (Persona에서 관리)
ALTER TABLE chat_sessions DROP COLUMN s3_key;
ALTER TABLE chat_sessions DROP COLUMN selected_speaker;
ALTER TABLE chat_sessions DROP COLUMN original_file_name;

-- system_prompt는 유지 (런타임 생성 후 캐싱용)
-- persona_id는 필수로 변경
ALTER TABLE chat_sessions MODIFY COLUMN persona_id BIGINT NOT NULL;

