CREATE TABLE persona_trait (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    persona_id BIGINT NOT NULL,
    trait_type ENUM('speech_pattern', 'habit_word', 'personality') NOT NULL,
    trait_value TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (persona_id) REFERENCES personas(id) ON DELETE CASCADE,
    INDEX idx_persona_id (persona_id)
);
