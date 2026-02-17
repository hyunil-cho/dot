package com.dot.backend.client.ai;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * AI API Mock 구현체 (테스트 전용)
 *
 * 실제 AI Engine 없이도 API 테스트 가능
 * 통합 테스트에서 직접 인스턴스 생성하여 사용
 */
@Slf4j
public class MockAiApiClient implements AiApiClient {

    private final ConcurrentMap<String, TrainingJobStatusResponse> jobs = new ConcurrentHashMap<>();

    @Override
    public TrainingJobResponse createTrainingJob(TrainingJobRequest request) {
        String jobId = "mock-job-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Mock: Creating training job for persona {} with {} voice files",
                request.getPersonaId(), request.getVoiceFileIds().size());

        // Mock 상태 저장
        TrainingJobStatusResponse status = TrainingJobStatusResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .progress(0)
                .createdAt(LocalDateTime.now())
                .estimatedCompletionTime(LocalDateTime.now().plusMinutes(5))
                .build();

        jobs.put(jobId, status);

        return TrainingJobResponse.builder()
                .jobId(jobId)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .estimatedDurationMinutes(5)
                .build();
    }

    @Override
    public TrainingJobStatusResponse getJobStatus(String jobId) {
        log.info("Mock: Getting job status for {}", jobId);

        TrainingJobStatusResponse status = jobs.get(jobId);

        if (status == null) {
            throw new IllegalArgumentException("Job not found: " + jobId);
        }

        // Mock: 시간 경과에 따라 상태 자동 변경
        updateMockStatus(status);

        return status;
    }

    @Override
    public void cancelJob(String jobId) {
        log.info("Mock: Cancelling job {}", jobId);
        jobs.remove(jobId);
    }

    @Override
    public String uploadVoiceFile(Long personaId, String fileUrl) {
        String fileId = "mock-file-" + UUID.randomUUID().toString().substring(0, 8);
        log.info("Mock: Uploading voice file for persona {}: {}", personaId, fileId);
        return fileId;
    }

    /**
     * Mock 상태 자동 업데이트 (시간 경과 시뮬레이션)
     */
    private void updateMockStatus(TrainingJobStatusResponse status) {
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = java.time.Duration.between(status.getCreatedAt(), now).toMinutes();

        if (minutesElapsed < 1) {
            status.setStatus("PENDING");
            status.setProgress(0);
        } else if (minutesElapsed < 3) {
            status.setStatus("PROCESSING");
            status.setProgress((int) (minutesElapsed * 30)); // 1분: 30%, 2분: 60%
            status.setStartedAt(status.getCreatedAt().plusMinutes(1));
        } else {
            status.setStatus("COMPLETED");
            status.setProgress(100);
            status.setCompletedAt(status.getCreatedAt().plusMinutes(3));
        }
    }
}

