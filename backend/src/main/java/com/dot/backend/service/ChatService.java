package com.dot.backend.service;

import com.dot.backend.client.GeminiApiClient;
import com.dot.backend.domain.chat.ChatMessage;
import com.dot.backend.domain.chat.repository.ChatMessageRepository;
import com.dot.backend.domain.chatsession.ChatSession;
import com.dot.backend.domain.chatsession.repository.ChatSessionRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.dto.chat.ChatMessageResponse;
import com.dot.backend.dto.chat.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 메시지 처리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final GeminiApiClient geminiApiClient;

    /**
     * 메���지 전송 및 AI 응답 생성
     *
     * @param user 현재 사용자
     * @param sessionId 채팅 세션 ID
     * @param request 메시지 내용
     * @return AI 응답
     */
    @Transactional
    public ChatMessageResponse sendMessage(User user, Long sessionId, SendMessageRequest request) {
        log.info("Sending message for session: {}, user: {}", sessionId, user.getEmail());

        // 1. ChatSession 조회 및 권한 확인
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        if (!session.isActive()) {
            throw new IllegalStateException("활성화된 세션이 아닙니다");
        }

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .user(user)
                .persona(session.getPersona())
                .role(ChatMessage.Role.USER)
                .content(request.getContent())
                .build();

        chatMessageRepository.save(userMessage);
        log.info("User message saved: {}", userMessage.getId());

        // 3. 이전 대화 이력 조회 (최근 10개)
        List<ChatMessage> recentMessages = chatMessageRepository
                .findByUserIdAndPersonaIdOrderByCreatedAtAsc(user.getId(), session.getPersona().getId())
                .stream()
                .skip(Math.max(0, chatMessageRepository
                        .findByUserIdAndPersonaIdOrderByCreatedAtAsc(user.getId(), session.getPersona().getId())
                        .size() - 11)) // 최근 10개 (방금 저장한 사용자 메시지 제외하고 9개 + 새 메시지 1개)
                .collect(Collectors.toList());

        // 4. Gemini API 호출용 대화 이력 변환 (사용자 메시지 제외)
        List<GeminiApiClient.ChatMessage> conversationHistory = recentMessages.stream()
                .filter(msg -> msg.getId() < userMessage.getId()) // 방금 저장한 메시지 제외
                .map(msg -> new GeminiApiClient.ChatMessage(
                        msg.getContent(),
                        msg.getRole() == ChatMessage.Role.USER
                ))
                .collect(Collectors.toList());

        // 5. Gemini API 호출
        String aiResponse;
        try {
            aiResponse = geminiApiClient.generateResponse(
                    session.getSystemPrompt(),
                    conversationHistory,
                    request.getContent()
            );
            log.info("AI response generated: {} characters", aiResponse.length());
        } catch (Exception e) {
            log.error("Failed to generate AI response", e);
            throw new RuntimeException("AI 응답 생성 실패: " + e.getMessage(), e);
        }

        // 6. AI 응답 메시지 저장
        ChatMessage assistantMessage = ChatMessage.builder()
                .user(user)
                .persona(session.getPersona())
                .role(ChatMessage.Role.ASSISTANT)
                .content(aiResponse)
                .build();

        chatMessageRepository.save(assistantMessage);
        log.info("Assistant message saved: {}", assistantMessage.getId());

        // 7. 응답 반환
        return toResponse(assistantMessage);
    }

    /**
     * 대화 이력 조회
     *
     * @param user 현재 사용자
     * @param sessionId 채팅 세션 ID
     * @return 메시지 목록
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(User user, Long sessionId) {
        log.debug("Fetching messages for session: {}, user: {}", sessionId, user.getEmail());

        // ChatSession 조회 및 권한 확인
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("채팅 세션을 찾을 수 없습니다"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("접근 권한이 없습니다");
        }

        // 메시지 조회
        List<ChatMessage> messages = chatMessageRepository
                .findByUserIdAndPersonaIdOrderByCreatedAtAsc(user.getId(), session.getPersona().getId());

        return messages.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ChatMessage → Response DTO 변환
     */
    private ChatMessageResponse toResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .content(message.getContent())
                .role(message.getRole().name())
                .isFromUser(message.getRole() == ChatMessage.Role.USER)
                .createdAt(message.getCreatedAt())
                .build();
    }
}

