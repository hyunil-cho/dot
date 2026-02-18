package com.dot.backend.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
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

    private static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-1.5-flash}")
    private String model;

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
        log.info("Calling Gemini API ({}) for message: {}", model, userMessage);

        try {
            // 1. Contents 구성 (역사 + 현재 메시지)
            List<Map<String, Object>> contents = new ArrayList<>();

            // 대화 이력 추가
            if (conversationHistory != null) {
                for (ChatMessage msg : conversationHistory) {
                    contents.add(Map.of(
                        "role", msg.isFromUser() ? "user" : "model",
                        "parts", List.of(Map.of("text", msg.getContent()))
                    ));
                }
            }

            // 현재 메시지 추가
            contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", userMessage))
            ));

            // 2. 요청 본문 생성 (System Instruction 포함)
            Map<String, Object> requestBody = Map.of(
                "system_instruction", Map.of(
                    "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", contents,
                "generationConfig", Map.of(
                    "temperature", 0.7,
                    "topK", 40,
                    "topP", 0.95,
                    "maxOutputTokens", 1024
                )
            );

            // 3. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // 4. API 호출
            String url = GEMINI_API_BASE_URL + model + ":generateContent?key=" + apiKey;
            
            log.debug("Gemini API Request URL: {}", url);
            log.debug("Gemini API Request Body: {}", requestBody);

            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            // 5. 응답 파싱
            String generatedText = extractTextFromResponse(response);

            log.info("Gemini API response received: {} characters", generatedText.length());
            return generatedText;

        } catch (Exception e) {
            log.error("Failed to call Gemini API. Model: {}, Error: {}", model, e.getMessage());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Gemini API 응답에서 텍스트 추출
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                throw new RuntimeException("API 응답이 null입니다");
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                // 에러 정보 확인
                if (response.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) response.get("error");
                    throw new RuntimeException("API Error: " + error.get("message"));
                }
                throw new RuntimeException("응답에 candidates가 없습니다");
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            
            // finishReason 확인 (SAFETY 등)
            if (firstCandidate.containsKey("finishReason") && !"STOP".equals(firstCandidate.get("finishReason"))) {
                log.warn("Gemini finishReason: {}", firstCandidate.get("finishReason"));
            }

            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            if (content == null) {
                throw new RuntimeException("응답에 content가 없습니다 (차단되었을 가능성)");
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("응답에 parts가 없습니다");
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response);
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
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

