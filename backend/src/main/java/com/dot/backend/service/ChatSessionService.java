package com.dot.backend.service;

import com.dot.backend.domain.chat.repository.ChatMessageRepository;
import com.dot.backend.domain.chatsession.ChatSession;
import com.dot.backend.domain.chatsession.ChatSessionStatus;
import com.dot.backend.domain.chatsession.dto.ChatSessionResponse;
import com.dot.backend.domain.chatsession.repository.ChatSessionRepository;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.dto.chat.ChatSessionListResponse;
import com.dot.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 채팅 세션 단건 조회
     */
    @Transactional(readOnly = true)
    public ChatSessionResponse getSession(User user, Long sessionId) {
        log.debug("Fetching chat session: {}, user: {}", sessionId, user.getEmail());

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        return toResponse(session);
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

        // 2. 페르소나 준비 상태 확인 (trait 존재 여부)
        if (!persona.isReadyForChat()) {
            throw new IllegalStateException("페르소나 지침서(Trait)가 생성되지 않았습니다. 다시 등록하거나 잠시 후 시도해주세요.");
        }

        // 3. 중복 세션 체크 (같은 유저 + 같은 페르소나로 ACTIVE 세션이 이미 있으면 막기)
        boolean alreadyExists = chatSessionRepository
                .existsByUserIdAndPersonaIdAndStatus(user.getId(), personaId, ChatSessionStatus.ACTIVE);

        if (alreadyExists) {
            throw new IllegalStateException("이미 활성화된 채팅 세션이 있습니다. 기존 세션을 종료 후 다시 시도해주세요.");
        }

        // 4. ChatSession 생성
        ChatSession session = ChatSession.builder()
                .user(user)
                .persona(persona)
                .status(ChatSessionStatus.INIT)
                .build();

        session = chatSessionRepository.save(session);

        // 5. 세션 시작 (Persona의 Trait을 시스템 프롬프트로 설정 및 ACTIVE 상태로 변경)
        session.start(persona.getTrait());

        log.info("Chat session created and activated: {}", session.getId());

        return toResponse(session);
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




