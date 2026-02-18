package com.dot.backend.service;

import com.dot.backend.domain.chat.repository.ChatMessageRepository;
import com.dot.backend.domain.chatsession.ChatSession;
import com.dot.backend.domain.chatsession.ChatSessionStatus;
import com.dot.backend.domain.chatsession.dto.ChatSessionResponse;
import com.dot.backend.domain.chatsession.repository.ChatSessionRepository;
import com.dot.backend.domain.persona.ConversationSample;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.ConversationSampleRepository;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.dto.chat.ChatSessionListResponse;
import com.dot.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final PersonaRepository personaRepository;
    private final ConversationSampleRepository conversationSampleRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final EncryptionUtil encryptionUtil;

    /**
     * 사용자의 모든 채팅 세션 목록 조회
     *
     * @param user 현재 사용자
     * @return List<ChatSessionListResponse>
     */
    @Transactional(readOnly = true)
    public List<ChatSessionListResponse> getSessions(User user) {
        log.debug("Fetching all chat sessions for user: {}", user.getEmail());

        List<ChatSession> sessions = chatSessionRepository.findByUserOrderByUpdatedAtDesc(user);

        return sessions.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * 채팅 세션 생성
     *
     * @param user 현재 사용자
     * @param personaId Persona ID
     * @return ChatSessionResponse
     */
    @Transactional
    public ChatSessionResponse createSession(User user, Long personaId) {
        log.info("Creating chat session for user: {}, persona: {}", user.getEmail(), personaId);

        // 1. Persona 조회
        Persona persona = personaRepository.findByIdAndUserId(personaId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Persona를 찾을 수 없습니다"));

        // 2. 중복 세션 체크 (같은 유저 + 같은 페르소나로 ACTIVE 세션이 이미 있으면 막기)
        boolean alreadyExists = chatSessionRepository
                .existsByUserIdAndPersonaIdAndStatus(user.getId(), personaId, ChatSessionStatus.ACTIVE);

        if (alreadyExists) {
            throw new IllegalStateException("이미 활성화된 채팅 세션이 있습니다. 기존 세션을 종료 후 다시 시도해주세요.");
        }
        // 2. ConversationSample 조회 (최근 50개)
        List<ConversationSample> samples = conversationSampleRepository
                .findByPersonaIdOrderByCreatedAtDesc(persona.getId(), PageRequest.of(0, 50));

        // 3. 시스템 프롬프트 생성
        String systemPrompt = buildSystemPrompt(persona, samples);

        // 4. ChatSession 생성
        ChatSession session = ChatSession.builder()
                .user(user)
                .persona(persona)
                .status(ChatSessionStatus.INIT)
                .build();

        session = chatSessionRepository.save(session);

        // 5. 세션 시작 (시스템 프롬프트 설정)
        session.start(systemPrompt);

        log.info("Chat session created: {}", session.getId());

        return toResponse(session);
    }

    /**
     * 시스템 프롬프트 생성
     *
     * Persona의 memo와 ConversationSample을 조합하여 Gemini에 전달할 프롬프트 생성
     */
    private String buildSystemPrompt(Persona persona, List<ConversationSample> samples) {
        StringBuilder prompt = new StringBuilder();

        // 복호화
        String personaName = encryptionUtil.decrypt(persona.getName());

        // 1. 기본 역할 정의
        prompt.append("당신은 '").append(personaName).append("'입니다.\n\n");

        // 2. 사용자 입력 메모 (성향, 특징)
        if (persona.getMemo() != null && !persona.getMemo().isBlank()) {
            prompt.append("=== 성향 및 특징 ===\n");
            prompt.append(persona.getMemo()).append("\n\n");
        }

        // 3. 대화 샘플 (Few-shot learning)
        if (!samples.isEmpty()) {
            prompt.append("=== 실제 대화 예시 ===\n");
            prompt.append("다음은 당신의 실제 대화 스타일입니다. 이를 참고하여 응답하세요.\n\n");

            // 최대 20개만 포함 (토큰 제한)
            int count = Math.min(samples.size(), 20);
            for (int i = 0; i < count; i++) {
                ConversationSample sample = samples.get(i);
                String speaker = sample.getRole() == ConversationSample.Role.PERSONA
                        ? personaName
                        : "상대방";
                prompt.append(speaker).append(": ").append(sample.getMessage()).append("\n");
            }

            prompt.append("\n");
        }

        // 4. 지시사항
        prompt.append("=== 중요 지시사항 ===\n");
        prompt.append("- 위 대화 스타일과 성향을 바탕으로 자연스럽게 응답하세요.\n");
        prompt.append("- 일관된 성격과 말투를 유지하세요.\n");
        prompt.append("- '").append(personaName).append("'의 입장에서 대화하세요.\n");

        return prompt.toString();
    }

    /**
     * ChatSession → Response DTO 변환
     */
    private ChatSessionResponse toResponse(ChatSession session) {
        return ChatSessionResponse.builder()
                .sessionId(session.getId())
                .personaId(session.getPersona().getId())
                .personaName(encryptionUtil.decrypt(session.getPersona().getName()))
                .status(session.getStatus().name())
                .startedAt(session.getStartedAt())
                .build();
    }
    
    /**
     * ChatSession -> List Response DTO 변환
     */
    private ChatSessionListResponse toListResponse(ChatSession session) {
        String lastMessage = chatMessageRepository
                .findFirstByPersonaIdAndUserIdOrderByCreatedAtDesc(session.getPersona().getId(), session.getUser().getId())
                .map(m -> m.getRole() == com.dot.backend.domain.chat.ChatMessage.Role.USER ? "나: " + m.getContent() : m.getContent())
                .orElse("아직 대화가 없습니다.");

        return ChatSessionListResponse.builder()
                .sessionId(session.getId())
                .personaId(session.getPersona().getId())
                .personaName(encryptionUtil.decrypt(session.getPersona().getName()))
                .lastMessage(lastMessage)
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}




