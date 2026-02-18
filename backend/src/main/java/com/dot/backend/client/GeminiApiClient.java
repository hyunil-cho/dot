package com.dot.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Google Gemini API 클라이언트
 *
 * API 문서: https://ai.google.dev/api/rest
 */
@Component
@Slf4j
public class GeminiApiClient {

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    @Value("${gemini.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GeminiApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Gemini API 호출
     *
     * @param systemPrompt 시스템 프롬프트 (Persona 특성)
     * @param conversationHistory 대화 이력
     * @param userMessage 사용자 메시지
     * @return AI 응답
     */
    public String generateResponse(
            String systemPrompt,
            List<ChatMessage> conversationHistory,
            String userMessage
    ) {
        log.info("Calling Gemini API for message: {}", userMessage);

        try {
            // 요청 본문 생성
            String fullPrompt = buildFullPrompt(systemPrompt, conversationHistory, userMessage);

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "parts", List.of(
                            Map.of("text", fullPrompt)
                        )
                    )
                )
            );

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // API 호출
            String url = GEMINI_API_URL + "?key=" + apiKey;
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            // 응답 파싱
            String generatedText = extractTextFromResponse(response);

            log.info("Gemini API response received: {} characters", generatedText.length());
            return generatedText;

        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 전체 프롬프트 구성
     */
    private String buildFullPrompt(
            String systemPrompt,
            List<ChatMessage> history,
            String userMessage
    ) {
        StringBuilder prompt = new StringBuilder();

        // 시스템 프롬프트 (Persona 특성)
        prompt.append("=== 역할 및 성향 ===\n");
        prompt.append(systemPrompt);
        prompt.append("\n\n");

        // 대화 이력
        if (history != null && !history.isEmpty()) {
            prompt.append("=== 이전 대화 ===\n");
            for (ChatMessage msg : history) {
                String speaker = msg.isFromUser() ? "사용자" : "당신";
                prompt.append(speaker).append(": ").append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }

        // 현재 사용자 메시지
        prompt.append("=== 새로운 메시지 ===\n");
        prompt.append("사용자: ").append(userMessage).append("\n\n");
        prompt.append("위 역할과 성향을 바탕으로 자연스럽게 응답하세요. 대화 이력을 참고하되, 일관된 성격을 유지하세요.");

        return prompt.toString();
    }

    /**
     * Gemini API 응답에서 텍스트 추출
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in response");
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("No parts in response");
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response);
            throw new RuntimeException("Gemini 응답 파싱 실패", e);
        }
    }

    /**
     * 대화 메시지 DTO (간단한 내부 클래스)
     */
    public static class ChatMessage {
        private final String content;
        private final boolean fromUser;

        public ChatMessage(String content, boolean fromUser) {
            this.content = content;
            this.fromUser = fromUser;
        }

        public String getContent() {
            return content;
        }

        public boolean isFromUser() {
            return fromUser;
        }
    }
}

