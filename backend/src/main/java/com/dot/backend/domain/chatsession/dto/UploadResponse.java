package com.dot.backend.domain.chatsession.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UploadResponse {
    private List<String> speakers; // 파싱된 화자 목록
}