package com.dot.backend.client.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 개발 환경용 간단한 Mock 구현체
 *
 * AI Engine이 준비되기 전까지 개발 서버에서 사용
 * 항상 성공 응답 반환
 */
@Slf4j
@Component
@Profile({"dev", "local"})
public class DevAiApiClient implements AiApiClient {

    @Override
    public TrainingJobResponse createTrainingJob(TrainingJobRequest request) {
        String jobId = "dev-job-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("[DEV] Mock training job created: {} for persona {}",
                jobId, request.getPersonaId());

        return TrainingJobResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .estimatedDurationMinutes(5)
                .build();
    }

    @Override
    public TrainingJobStatusResponse getJobStatus(String jobId) {
        log.info("[DEV] Mock job status: {} - COMPLETED", jobId);

        return TrainingJobStatusResponse.builder()
                .jobId(jobId)
                .status("COMPLETED")
                .progress(100)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .completedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public void cancelJob(String jobId) {
        log.info("[DEV] Mock job cancelled: {}", jobId);
    }

    @Override
    public String uploadVoiceFile(Long personaId, String fileUrl) {
        String fileId = "dev-file-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("[DEV] Mock voice file uploaded: {} for persona {}", fileId, personaId);
        return fileId;
    }
}

