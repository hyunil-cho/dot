package com.dot.backend.parser;

import com.dot.backend.client.GeminiApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class KakaoTxtParser {

    private final GeminiApiClient geminiApiClient;

    // 카카오톡 메시지 패턴
    // 예: "2024년 1월 15일 오전 10:23, 홍길동 : 안녕하세요"
    // 안드로이드: "2024년 1월 15일 오전 10:23, 홍길동 : 안녕"
    private static final Pattern ANDROID_PATTERN = Pattern.compile(
            "^(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (오전|오후) \\d{1,2}:\\d{2}), (.+?) : (.+)$"
    );

    // 아이폰: "2024-01-15 10:23:45 홍길동 : 안녕"
    private static final Pattern IOS_PATTERN = Pattern.compile(
            "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) (.+?) : (.+)$"
    );

    // 시스템 메시지 패턴 (입장, 퇴장 등 - 무시할 것들)
    private static final List<String> SYSTEM_KEYWORDS = List.of(
            "님이 들어왔습니다",
            "님이 나갔습니다",
            "님을 초대했습니다",
            "저장한 날짜",
            "채팅방 멤버"
    );

    /**
     * TXT 파일을 파싱해서 메시지 목록 반환 (MultipartFile 버전)
     */
    public List<ParsedMessage> parse(MultipartFile file) throws IOException {
        return parse(file.getInputStream());
    }

    /**
     * TXT 파일을 파싱해서 메시지 목록 반환 (InputStream 버전)
     */
    public List<ParsedMessage> parse(InputStream inputStream) throws IOException {
        List<ParsedMessage> messages = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 빈 줄 스킵
                if (line.isEmpty()) continue;

                // 시스템 메시지 스킵
                if (isSystemMessage(line)) continue;

                // 메시지 파싱 시도
                Matcher androidMatcher = ANDROID_PATTERN.matcher(line);
                Matcher iosMatcher = IOS_PATTERN.matcher(line);

                if (androidMatcher.matches()) {
                    String sentAt = androidMatcher.group(1);
                    String speaker = androidMatcher.group(3);
                    String content = androidMatcher.group(4);
                    messages.add(new ParsedMessage(speaker, content, sentAt));

                } else if (iosMatcher.matches()) {
                    String sentAt = iosMatcher.group(1);
                    String speaker = iosMatcher.group(2);
                    String content = iosMatcher.group(3);
                    messages.add(new ParsedMessage(speaker, content, sentAt));
                }
            }
        }

        return messages;
    }

    /**
     * 파싱된 메시지에서 화자 목록만 추출 (중복 제거, 순서 유지)
     */
    public List<String> extractSpeakers(List<ParsedMessage> messages) {
        // LinkedHashSet: 중복 제거하면서 처음 등장 순서 유지
        Set<String> speakers = new LinkedHashSet<>();
        for (ParsedMessage message : messages) {
            speakers.add(message.getSpeaker());
        }
        return new ArrayList<>(speakers);
    }

    /**
     * 시스템 메시지 여부 확인
     */
    private boolean isSystemMessage(String line) {
        for (String keyword : SYSTEM_KEYWORDS) {
            if (line.contains(keyword)) return true;
        }
        return false;
    }

    /**
     * 페르소나의 대화 스타일과 특징을 분석하여 Gemini 지침서(Trait) 생성
     */
    public String analyzeAndGenerateTrait(
            String personaName,
            String userName,
            String relationship,
            String memo,
            List<ParsedMessage> messages,
            String speakerName
    ) {
        log.info("Analyzing conversation between {} and {} to generate trait", speakerName, userName);

        // 1. 분석을 위한 데이터 준비 (상대방과의 대화 흐름을 파악하기 위해 전체 샘플링)
        List<String> samples = messages.stream()
                .limit(150) // 분석 정확도를 위해 샘플 수 약간 증가
                .map(m -> m.getSpeaker() + ": " + m.getContent())
                .collect(Collectors.toList());

        String conversationData = String.join("\n", samples);

        // 2. Gemini용 시스템 프롬프트 작성
        String analyzerSystemPrompt = 
                "당신은 전문적인 언어 분석가이자 캐릭터 디자이너입니다. " +
                "제공된 카카오톡 대화 데이터와 메타데이터를 분석하여, 해당 인물의 말투, 성격, 특징을 포함한 페르소나 지침서(Trait)를 작성해야 합니다.\n\n" +
                "특히 다음 사항에 집중하세요:\n" +
                "1. 호칭 분석: 페르소나가 사용자를 어떻게 부르는지 대화 데이터에서 찾아내세요. (예: '~형님', '형', '~씨', '~아(야)', '아들' 등)\n" +
                "2. 관계 기반 문체: 사용자와의 관계(" + relationship + ")에 따른 예의의 정도와 친밀함을 반영하세요.\n" +
                "3. 이름 사용 규칙: 성을 붙이는지, 이름만 부르는지, 별명을 쓰는지 파악하세요.\n\n" +
                "작성 형식은 반드시 다음 형식을 따라야 합니다:\n" +
                "# Role\n[인물의 역할, 별명, 연령대, 성격 요약]\n" +
                "# Style & Tone\n" +
                "- 호칭: [사용자를 부르는 구체적인 호칭 규칙. 예: '이름만 부르고 뒤에 형님을 붙임', '성을 떼고 이름만 부름' 등]\n" +
                "- 문체: [말투의 특징, 종결 어미, 이모티콘 사용 여부 등]\n" +
                "- 리액션: [상대방의 말에 반응하는 방식]\n" +
                "- 성격: [핵심적인 성격적 특징]\n" +
                "# Context (User Specifics)\n" +
                "- 사용자 이름: " + userName + "\n" +
                "- 관계: " + relationship + "\n" +
                "- 대화 배경: [사용자와의 관계 및 현재 상황]\n" +
                "# Constraints\n" +
                "- 사용자를 부를 때는 반드시 분석된 호칭 규칙을 따르십시오.\n" +
                "- AI임을 드러내지 말고 실제 사람처럼 자연스럽게 대화하십시오.";

        // 3. 분석 요청 메시지 작성
        StringBuilder userRequest = new StringBuilder();
        userRequest.append("다음 정보를 바탕으로 페르소나 지침서를 작성해줘.\n\n");
        userRequest.append("[기본 정보]\n");
        userRequest.append("- 페르소나 이름: ").append(personaName).append("\n");
        userRequest.append("- 사용자 이름(나): ").append(userName).append("\n");
        userRequest.append("- 사용자와의 관계: ").append(relationship != null ? relationship : "정보 없음").append("\n");
        userRequest.append("- 메모/특이사항: ").append(memo != null ? memo : "정보 없음").append("\n");
        userRequest.append("- 분석 대상 화자 이름: ").append(speakerName).append("\n\n");
        userRequest.append("[대화 데이터 샘플]\n");
        userRequest.append(conversationData);

        // 4. Gemini API 호출
        try {
            return geminiApiClient.generateResponse(analyzerSystemPrompt, List.of(), userRequest.toString());
        } catch (Exception e) {
            log.error("Failed to generate persona trait via Gemini API", e);
            // 실패 시 기본 템플릿이라도 반환 (또는 예외 던지기)
            return String.format("# Role\n당신은 %s입니다.\n# Style & Tone\n- 호칭: 사용자\n- 문체: 자연스러운 말투\n# Context\n%s와의 관계\n# Constraints\n친절하게 대답하세요.", personaName, relationship);
        }
    }
}
