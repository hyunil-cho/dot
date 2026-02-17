package com.dot.backend.client.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Engine 학습 상태 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingJobStatusResponse {

    private String jobId;

    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    private Integer progress; // 0~100 (진행률)

    private LocalDateTime estimatedCompletionTime; // 예상 완료 시간

    private String errorMessage; // 실패 시 에러 메시지

    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
}

