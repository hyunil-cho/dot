package com.dot.backend.controller;

import com.dot.backend.domain.chatsession.dto.ChatSessionResponse;
import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.chat.ChatMessageResponse;
import com.dot.backend.dto.chat.ChatSessionListResponse;
import com.dot.backend.dto.chat.SendMessageRequest;
import com.dot.backend.service.ChatService;
import com.dot.backend.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * 채팅 API 컨트롤러
 *
 * - POST /api/chat-sessions - 채팅 세션 생성
 * - GET /api/chat-sessions - 채팅 세션 목록 조회
 * - POST /api/chat-sessions/{sessionId}/messages - 메시지 전송
 * - GET /api/chat-sessions/{sessionId}/messages - 대화 이력 조회
 */
@RestController
@RequestMapping("/api/chat-sessions")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "채팅 API")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatSessionService chatSessionService;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(
        summary = "채팅 세션 생성",
        description = "Persona와의 채팅 세션을 시작합니다.\n\n" +
            "**처리 과정:**\n" +
            "1. Persona 조회 (권한 확인)\n" +
            "2. ConversationSample 조회 (대화 샘플)\n" +
            "3. System Prompt 생성 (Few-shot Learning)\n" +
            "   - Persona 역할 정의\n" +
            "   - 성향 및 특징 (memo)\n" +
            "   - 실제 대화 예시 (ConversationSample)\n" +
            "4. ChatSession 생성 및 저장\n\n" +
            "**반환된 sessionId로 메시지를 주고받을 수 있습니다.**\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "세션 생성 성공",
            content = @Content(
                schema = @Schema(implementation = ChatSessionResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"sessionId\": 123,\n" +
                        "  \"personaId\": 1,\n" +
                        "  \"personaName\": \"엄마\",\n" +
                        "  \"status\": \"ACTIVE\",\n" +
                        "  \"startedAt\": \"2026-02-18T10:30:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (Persona 없음, ConversationSample 없음)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"Persona를 찾을 수 없습니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<ChatSessionResponse> createSession(
        @Parameter(description = "Persona ID", required = true, example = "1")
        @RequestParam("personaId") Long personaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        ChatSessionResponse response = chatSessionService.createSession(currentUser, personaId);

        return ResponseEntity
                .created(URI.create("/api/chat-sessions/" + response.getSessionId()))
                .body(response);
    }
    
    @GetMapping
    @Operation(
        summary = "채팅 세션 목록 조회",
        description = "사용자의 모든 채팅 세션 목록을 최신순으로 조회합니다.\n\n" +
            "**반환 정보:**\n" +
            "- `sessionId`: 채팅 세션 ID\n" +
            "- `personaId`: 페르소나 ID\n" +
            "- `personaName`: 페르소나 이름\n" +
            "- `lastMessage`: 마지막 대화 내용 (상대방 메시지는 그대로, 내 메시지는 '나: ' 접두사 추가)\n" +
            "- `updatedAt`: 마지막 대화 시간\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                array = @ArraySchema(schema = @Schema(implementation = ChatSessionListResponse.class)),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"sessionId\": 124,\n" +
                        "    \"personaId\": 2,\n" +
                        "    \"personaName\": \"친구\",\n" +
                        "    \"lastMessage\": \"나: 저녁에 뭐해?\",\n" +
                        "    \"updatedAt\": \"2026-02-18T18:00:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"sessionId\": 123,\n" +
                        "    \"personaId\": 1,\n" +
                        "    \"personaName\": \"엄마\",\n" +
                        "    \"lastMessage\": \"어, 왔니? 밥은 먹었어?\",\n" +
                        "    \"updatedAt\": \"2026-02-18T10:30:05\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<List<ChatSessionListResponse>> getSessions(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        List<ChatSessionListResponse> response = chatSessionService.getSessions(currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    @Operation(
            summary = "채팅 세션 단건 조회",
            description = "세션 ID로 특정 채팅 세션의 메타데이터를 조회합니다.\n\n" +
                    "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ChatSessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션 없음 또는 권한 없음")
    })
    public ResponseEntity<ChatSessionResponse> getSession(
            @Parameter(description = "채팅 세션 ID", required = true, example = "1")
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        ChatSessionResponse response = chatSessionService.getSession(currentUser, sessionId);
        return ResponseEntity.ok(response);
    }

    /**
     * 메시지 전송 및 AI 응답 받기
     */
    @PostMapping("/{sessionId}/messages")
    @Operation(
        summary = "메시지 전송",
        description = "채팅 세션에 메시지를 전송하고 AI(Persona) 응답을 받습니다.\n\n" +
            "**처리 과정:**\n" +
            "1. 사용자 메시지 저장 (ChatMessage, Role.USER)\n" +
            "2. 이전 대화 이력 조회 (최근 10개)\n" +
            "3. Gemini API 호출\n" +
            "   - System Prompt (세션 생성 시 저장된 프롬프트)\n" +
            "   - Conversation History (이전 대화)\n" +
            "   - User Message (현재 메시지)\n" +
            "4. AI 응답 저장 (ChatMessage, Role.ASSISTANT)\n" +
            "5. AI 응답 반환\n\n" +
            "**AI는 Persona의 성향과 대화 샘플을 학습하여 응답합니다.**\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "메시지 전송 성공, AI 응답 받음",
            content = @Content(
                schema = @Schema(implementation = ChatMessageResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"messageId\": 456,\n" +
                        "  \"content\": \"안녕! 오늘 날씨 정말 좋지? 산책이라도 나가볼까?\",\n" +
                        "  \"role\": \"ASSISTANT\",\n" +
                        "  \"isFromUser\": false,\n" +
                        "  \"createdAt\": \"2026-02-18T10:32:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (세션 없음, 권한 없음)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"접근 권한이 없습니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "AI 응답 생성 실패",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"AI 응답 생성 실패\"}"
                )
            )
        )
    })
    public ResponseEntity<ChatMessageResponse> sendMessage(
        @Parameter(description = "채팅 세션 ID", required = true, example = "123")
        @PathVariable Long sessionId,
        @Valid @RequestBody SendMessageRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        ChatMessageResponse response = chatService.sendMessage(currentUser, sessionId, request);

        return ResponseEntity.ok(response);
    }

    /**
     * 대화 이력 조회
     */
    @GetMapping("/{sessionId}/messages")
    @Operation(
        summary = "대화 이력 조회",
        description = "채팅 세션의 모든 메시지를 조회합니다.\n\n" +
            "**반환 형식:**\n" +
            "- 시간순 정렬 (오래된 것부터)\n" +
            "- 사용자 메시지 (role: USER, isFromUser: true)\n" +
            "- AI 응답 (role: ASSISTANT, isFromUser: false)\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                schema = @Schema(implementation = ChatMessageResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"messageId\": 1,\n" +
                        "    \"content\": \"안녕하세요!\",\n" +
                        "    \"role\": \"USER\",\n" +
                        "    \"isFromUser\": true,\n" +
                        "    \"createdAt\": \"2026-02-18T10:30:00\"\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"messageId\": 2,\n" +
                        "    \"content\": \"어, 왔니? 밥은 먹었어?\",\n" +
                        "    \"role\": \"ASSISTANT\",\n" +
                        "    \"isFromUser\": false,\n" +
                        "    \"createdAt\": \"2026-02-18T10:30:05\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (세션 없음, 권한 없음)"
        )
    })
    public ResponseEntity<List<ChatMessageResponse>> getMessages(
        @Parameter(description = "채팅 세션 ID", required = true, example = "123")
        @PathVariable Long sessionId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        List<ChatMessageResponse> messages = chatService.getMessages(currentUser, sessionId);

        return ResponseEntity.ok(messages);
    }

    /**
     * 현재 사용자 조회
     */
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다"));
    }
}

