package com.dot.backend.parser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ParsedMessage {
    private String speaker;   // 화자 이름 (예: "홍길동")
    private String content;   // 메시지 내용 (예: "안녕하세요")
    private String sentAt;    // 보낸 시간 문자열 (예: "2024년 1월 15일 오전 10:23")
}
