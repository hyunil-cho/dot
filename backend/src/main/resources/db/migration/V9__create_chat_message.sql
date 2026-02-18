CREATE TABLE chat_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    persona_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role ENUM('user', 'assistant') NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_persona_user (persona_id, user_id),
    INDEX idx_created_at (created_at)
);
