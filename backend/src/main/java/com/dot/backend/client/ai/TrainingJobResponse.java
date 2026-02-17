package com.dot.backend.client.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI Engine 학습 작업 생성 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingJobResponse {

    private String jobId; // AI Engine에서 생성한 작업 ID

    private String status; // PENDING, PROCESSING, COMPLETED, FAILED

    private LocalDateTime createdAt;

    private Integer estimatedDurationMinutes; // 예상 소요 시간 (분)
}

