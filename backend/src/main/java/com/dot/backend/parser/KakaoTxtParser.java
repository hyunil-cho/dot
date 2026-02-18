package com.dot.backend.parser;

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

@Component
public class KakaoTxtParser {

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
}
