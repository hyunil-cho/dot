package com.dot.backend.client.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 실제 AI API 클라이언트 구현체 (프로덕션용)
 *
 * RestTemplate을 사용한 HTTP 통신
 * TODO: 실제 AI Engine 엔드포인트 연동 필요
 */
@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class AiApiClientImpl implements AiApiClient {

    private final RestTemplate restTemplate;

    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;

    @Override
    public TrainingJobResponse createTrainingJob(TrainingJobRequest request) {
        // TODO: 실제 AI API 호출 구현
        String url = aiApiBaseUrl + "/training/jobs";

        log.info("Creating training job for persona {}", request.getPersonaId());

        // TrainingJobResponse response = restTemplate.postForObject(
        //     url,
        //     request,
        //     TrainingJobResponse.class
        // );

        throw new UnsupportedOperationException("AI API 연동 미구현 - AI Engine 준비 후 구현 필요");
    }

    @Override
    public TrainingJobStatusResponse getJobStatus(String jobId) {
        // TODO: 실제 AI API 호출 구현
        String url = aiApiBaseUrl + "/training/jobs/" + jobId;

        log.info("Getting job status for {}", jobId);

        throw new UnsupportedOperationException("AI API 연동 미구현 - AI Engine 준비 후 구현 필요");
    }

    @Override
    public void cancelJob(String jobId) {
        // TODO: 실제 AI API 호출 구현
        String url = aiApiBaseUrl + "/training/jobs/" + jobId + "/cancel";

        log.info("Cancelling job {}", jobId);

        throw new UnsupportedOperationException("AI API 연동 미구현 - AI Engine 준비 후 구현 필요");
    }

    @Override
    public String uploadVoiceFile(Long personaId, String fileUrl) {
        // TODO: 실제 AI API 호출 구현
        String url = aiApiBaseUrl + "/voice/upload";

        log.info("Uploading voice file for persona {}", personaId);

        throw new UnsupportedOperationException("AI API 연동 미구현 - AI Engine 준비 후 구현 필요");
    }
}

