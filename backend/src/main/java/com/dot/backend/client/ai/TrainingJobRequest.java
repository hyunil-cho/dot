package com.dot.backend.client.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI Engine 학습 작업 생성 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingJobRequest {

    private Long personaId;
    
    private List<String> voiceFileIds; // AI Engine에 업로드된 파일 ID 목록
    
    private String personaMemo; // AI가 참조할 메모
}

