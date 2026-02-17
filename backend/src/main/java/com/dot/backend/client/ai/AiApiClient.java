package com.dot.backend.client.ai;

/**
 * AI Engine API 클라이언트 인터페이스
 *
 * 실제 구현체:
 * - AiApiClientImpl: RestTemplate/WebClient를 사용한 HTTP 클라이언트
 * - MockAiApiClient: 테스트용 Mock 구현체
 */
public interface AiApiClient {

    /**
     * 학습 작업 생성
     *
     * @param request 학습 요청 정보
     * @return 생성된 작업 정보 (jobId 포함)
     */
    TrainingJobResponse createTrainingJob(TrainingJobRequest request);

    /**
     * 학습 상태 조회
     *
     * @param jobId AI Engine 작업 ID
     * @return 현재 학습 상태
     */
    TrainingJobStatusResponse getJobStatus(String jobId);

    /**
     * 학습 작업 취소
     *
     * @param jobId AI Engine 작업 ID
     */
    void cancelJob(String jobId);

    /**
     * 음성 파일을 AI Engine에 업로드
     *
     * @param personaId Persona ID
     * @param fileUrl S3 파일 URL
     * @return AI Engine에서 생성한 파일 ID
     */
    String uploadVoiceFile(Long personaId, String fileUrl);
}

