package com.dot.backend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 암호화 유틸리티
 *
 * 개인정보 (이름, 전화번호) 암호화/복호화에 사용
 */
@Component
@Slf4j
public class EncryptionUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom;

    public EncryptionUtil(@Value("${encryption.key}") String key) {
        // 키가 32바이트(256비트)인지 확인
        if (key.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new IllegalArgumentException("Encryption key must be 32 bytes (256 bits)");
        }

        this.secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        this.secureRandom = new SecureRandom();

        log.info("EncryptionUtil initialized with AES-256-GCM");
    }

    /**
     * 데이터 암호화
     *
     * @param plainText 평문
     * @return Base64 인코딩된 암호문 (IV + 암호문)
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            // IV 생성 (랜덤)
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // 암호화
            byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = cipher.doFinal(plainTextBytes);

            // IV + 암호문 결합
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            // Base64 인코딩
            return Base64.getEncoder().encodeToString(byteBuffer.array());

        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * 데이터 복호화
     *
     * @param encryptedText Base64 인코딩된 암호문 (IV + 암호문)
     * @return 평문
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            // Base64 디코딩
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);

            // IV와 암호문 분리
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedBytes);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            // Cipher 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // 복호화
            byte[] plainTextBytes = cipher.doFinal(cipherText);

            return new String(plainTextBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }

    /**
     * 암호화 여부 확인 (간단한 휴리스틱)
     *
     * @param text 확인할 텍스트
     * @return 암호화되어 있으면 true
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        try {
            // Base64 디코딩 시도
            byte[] decoded = Base64.getDecoder().decode(text);
            // 최소 길이 확인 (IV + 암호문 + 태그)
            return decoded.length >= (GCM_IV_LENGTH + GCM_TAG_LENGTH / 8);
        } catch (Exception e) {
            return false;
        }
    }
}

