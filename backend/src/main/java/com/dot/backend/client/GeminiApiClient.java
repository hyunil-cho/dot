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

    private static final String GEMINI_API_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/";

    @Value("${gemini.api-key}")
    private String apiKey;

    // ✅ 기본 모델 변경
    @Value("${gemini.model:gemini-2.0-flash}")
    private String model;

    private final RestTemplate restTemplate;

    public GeminiApiClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateResponse(
            String systemPrompt,
            List<ChatMessage> conversationHistory,
            String userMessage
    ) {
        log.info("Calling Gemini API ({}) for message: {}", model, userMessage);

        try {
            List<Map<String, Object>> contents = new ArrayList<>();

            // System Prompt
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(
                            Map.of("text", systemPrompt)
                    )
            ));

            // Conversation History
            if (conversationHistory != null) {
                for (ChatMessage msg : conversationHistory) {
                    contents.add(Map.of(
                            "role", msg.isFromUser() ? "user" : "model",
                            "parts", List.of(
                                    Map.of("text", msg.getContent())
                            )
                    ));
                }
            }

            // Current User Message
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(
                            Map.of("text", userMessage)
                    )
            ));

            Map<String, Object> requestBody = Map.of(
                    "contents", contents,
                    "generationConfig", Map.of(
                            "temperature", 0.3,
                            "topK", 40,
                            "topP", 0.95,
                            "maxOutputTokens", 1024
                    )
            );

            // ✅ 헤더에 API Key 추가
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(requestBody, headers);

            // ✅ 쿼리스트링 key 제거
            String url = GEMINI_API_BASE_URL + model + ":generateContent";

            log.debug("Gemini API Request URL: {}", url);
            log.debug("Gemini API Request Body: {}", requestBody);

            Map<String, Object> response =
                    restTemplate.postForObject(url, request, Map.class);

            String generatedText = extractTextFromResponse(response);

            log.info("Gemini API response received: {} characters",
                    generatedText != null ? generatedText.length() : 0);

            return generatedText;

        } catch (Exception e) {
            log.error("Failed to call Gemini API. Model: {}, Error: {}", model, e.getMessage());
            throw new RuntimeException("Gemini API 호출 실패: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<String, Object> response) {
        try {
            if (response == null) {
                throw new RuntimeException("API 응답이 null입니다");
            }

            List<Map<String, Object>> candidates =
                    (List<Map<String, Object>>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                if (response.containsKey("error")) {
                    Map<String, Object> error =
                            (Map<String, Object>) response.get("error");
                    throw new RuntimeException("API Error: " + error.get("message"));
                }
                throw new RuntimeException("응답에 candidates가 없습니다");
            }

            Map<String, Object> firstCandidate = candidates.get(0);

            if (firstCandidate.containsKey("finishReason") &&
                    !"STOP".equals(firstCandidate.get("finishReason"))) {
                log.warn("Gemini finishReason: {}",
                        firstCandidate.get("finishReason"));
            }

            Map<String, Object> content =
                    (Map<String, Object>) firstCandidate.get("content");

            if (content == null) {
                throw new RuntimeException("응답에 content가 없습니다");
            }

            List<Map<String, Object>> parts =
                    (List<Map<String, Object>>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException("응답에 parts가 없습니다");
            }

            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", response);
            throw new RuntimeException("Gemini 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

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
