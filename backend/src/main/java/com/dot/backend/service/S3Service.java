package com.dot.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * S3 파일 업로드/삭제 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:dot-bucket}")
    private String bucketName;

    @Value("${aws.region:ap-northeast-2}")
    private String region;

    /**
     * 프로필 이미지 업로드
     *
     * @param file 업로드할 이미지 파일
     * @param userId 사용자 ID (폴더 구분용)
     * @return S3 URL
     */
    public String uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 파일명 생성: profiles/{userId}/{UUID}.{extension}
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String key = String.format("profiles/%d/%s%s", userId, UUID.randomUUID(), extension);

        log.info("Uploading profile image to S3: bucket={}, key={}", bucketName, key);

        try {
            // S3에 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // URL 생성
            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            log.info("Profile image uploaded successfully: {}", url);

            return url;
        } catch (Exception e) {
            log.error("Failed to upload profile image to S3", e);
            throw new RuntimeException("프로필 이미지 업로드 실패", e);
        }
    }

    /**
     * 카톡 파일 업로드
     *
     * @param file 업로드할 텍스트 파일
     * @param userId 사용자 ID
     * @return S3 Key (경로)
     */
    public String uploadKakaoFile(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일명 생성: kakao/{userId}/{UUID}.txt
        String key = String.format("kakao/%d/%s.txt", userId, UUID.randomUUID());

        log.info("Uploading kakao file to S3: bucket={}, key={}", bucketName, key);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/plain; charset=UTF-8")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("Kakao file uploaded successfully: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload kakao file to S3", e);
            throw new RuntimeException("카톡 파일 업로드 실패", e);
        }
    }

    /**
     * S3 파일 삭제
     *
     * @param key S3 key
     */
    public void deleteFile(String key) {
        if (key == null || key.isEmpty()) {
            return;
        }

        log.info("Deleting file from S3: bucket={}, key={}", bucketName, key);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", key, e);
            // 삭제 실패해도 로그만 남기고 예외는 던지지 않음
        }
    }

    /**
     * URL에서 S3 key 추출
     *
     * @param url S3 URL
     * @return S3 key
     */
    public String extractKeyFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // https://bucket.s3.region.amazonaws.com/key 형식에서 key 추출
        String[] parts = url.split(".amazonaws.com/");
        return parts.length > 1 ? parts[1] : null;
    }
}

