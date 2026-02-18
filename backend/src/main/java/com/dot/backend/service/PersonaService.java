package com.dot.backend.service;

import com.dot.backend.domain.persona.ConversationSample;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.dto.persona.*;
import com.dot.backend.parser.KakaoTxtParser;
import com.dot.backend.parser.ParsedMessage;
import com.dot.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Persona 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final EncryptionUtil encryptionUtil;
    private final S3Service s3Service;
    private final KakaoTxtParser kakaoTxtParser;

    /**
     * Persona 생성 (Multipart Form 방식)
     *
     * @param user 현재 사용자
     * @param name 이름
     * @param phoneNumber 전화번호
     * @param relationship 관계
     * @param memo 메모
     * @param profileImage 프로필 이미지 파일
     * @param kakaoFile 카톡 대화 파일
     * @param speakerName 화자 이름 (카톡 파일에서 Persona에 해당하는 화자)
     * @return PersonaResponse
     */
    @Transactional
    public PersonaResponse createPersona(
            User user,
            String name,
            String phoneNumber,
            String relationship,
            String memo,
            MultipartFile profileImage,
            MultipartFile kakaoFile,
            String speakerName
    ) {
        log.info("Creating persona for user: {}, name: {}, speakerName: {}", user.getEmail(), name, speakerName);

        // 전화번호 암호화 (있는 경우)
        String encryptedPhoneNumber = null;
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            encryptedPhoneNumber = encryptionUtil.encrypt(phoneNumber);
        }

        // 이름 암호화
        String encryptedName = encryptionUtil.encrypt(name);

        // 프로필 이미지 업로드 (있는 경우)
        String profileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                profileImageUrl = s3Service.uploadProfileImage(profileImage, user.getId());
                log.info("Profile image uploaded: {}", profileImageUrl);
            } catch (IOException e) {
                log.error("Failed to upload profile image", e);
                throw new RuntimeException("프로필 이미지 업로드 실패", e);
            }
        }

        // Persona 생성
        Persona persona = Persona.builder()
                .user(user)
                .name(encryptedName)
                .phoneNumber(encryptedPhoneNumber)
                .relationship(relationship)
                .memo(memo)
                .profileImageUrl(profileImageUrl)
                .isDeleted(false)
                .build();

        Persona savedPersona = personaRepository.save(persona);

        // 카톡 파일 처리 (있는 경우)
        if (kakaoFile != null && !kakaoFile.isEmpty()) {
            try {
                processKakaoFile(savedPersona, kakaoFile, speakerName);
            } catch (IOException e) {
                log.error("Failed to process kakao file", e);
                // 카톡 파일 처리 실패해도 Persona는 생성됨 (나중에 추가 가능)
            }
        }

        log.info("Persona created with ID: {}", savedPersona.getId());

        return toResponse(savedPersona);
    }

    /**
     * 카톡 파일 파싱 및 ConversationSample 저장
     */
    private void processKakaoFile(Persona persona, MultipartFile kakaoFile, String speakerName) throws IOException {
        log.info("Processing kakao file for persona: {}, speakerName: {}", persona.getId(), speakerName);

        // 파일 파싱
        List<ParsedMessage> messages = kakaoTxtParser.parse(kakaoFile);
        log.info("Parsed {} messages from kakao file", messages.size());

        // 화자 이름 검증
        boolean speakerFound = messages.stream()
                .anyMatch(msg -> msg.getSpeaker().equals(speakerName));

        if (!speakerFound) {
            throw new IllegalArgumentException(
                    String.format("화자 '%s'를 찾을 수 없습니다. 파일에서 발견된 화자: %s",
                            speakerName,
                            messages.stream().map(ParsedMessage::getSpeaker).distinct().collect(Collectors.joining(", ")))
            );
        }

        // ConversationSample로 변환 및 저장
        List<ConversationSample> samples = messages.stream()
                .map(msg -> {
                    // 화자 이름에 따라 Role 결정
                    ConversationSample.Role role = msg.getSpeaker().equals(speakerName)
                            ? ConversationSample.Role.PERSONA
                            : ConversationSample.Role.USER;

                    return ConversationSample.builder()
                            .persona(persona)
                            .role(role)
                            .message(msg.getContent())
                            .build();
                })
                .collect(Collectors.toList());

        // Persona의 samples에 추가 (CascadeType.ALL로 자동 저장)
        persona.getSamples().addAll(samples);

        log.info("Saved {} conversation samples for persona: {}", samples.size(), persona.getId());
    }

    /**
     * 사용자의 모든 Persona 조회 (목록)
     */
    public List<PersonaListResponse> getPersonaList(User user) {
        log.debug("Fetching persona list for user: {}", user.getEmail());

        List<Persona> personas = personaRepository.findActiveByUserId(user.getId());

        return personas.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    /**
     * Persona 상세 조회
     */
    public PersonaResponse getPersonaDetail(User user, Long personaId) {
        log.debug("Fetching persona detail: {} for user: {}", personaId, user.getEmail());

        Persona persona = personaRepository.findByIdAndUserId(personaId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Persona를 찾을 수 없습니다"));

        return toResponse(persona);
    }

    /**
     * Persona 수정 (Multipart Form 방식)
     */
    @Transactional
    public PersonaResponse updatePersona(
            User user,
            Long personaId,
            String name,
            String phoneNumber,
            String relationship,
            String memo,
            MultipartFile profileImage
    ) {
        log.info("Updating persona: {} for user: {}", personaId, user.getEmail());

        Persona persona = personaRepository.findByIdAndUserId(personaId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Persona를 찾을 수 없습니다"));

        // 이름 변경 시 암호화
        String encryptedName = null;
        if (name != null && !name.isBlank()) {
            encryptedName = encryptionUtil.encrypt(name);
        }

        // 전화번호 변경 시 암호화
        String encryptedPhoneNumber = null;
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            encryptedPhoneNumber = encryptionUtil.encrypt(phoneNumber);
        }

        // 프로필 업데이트
        persona.updateProfile(encryptedName, encryptedPhoneNumber, relationship, memo);

        // 프로필 이미지 업데이트 (파일이 있는 경우)
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 기존 이미지 삭제
                if (persona.getProfileImageUrl() != null) {
                    String oldKey = s3Service.extractKeyFromUrl(persona.getProfileImageUrl());
                    s3Service.deleteFile(oldKey);
                }

                // 새 이미지 업로드
                String newImageUrl = s3Service.uploadProfileImage(profileImage, user.getId());
                persona.updateProfileImage(newImageUrl);
                log.info("Profile image updated: {}", newImageUrl);
            } catch (IOException e) {
                log.error("Failed to upload profile image", e);
                throw new RuntimeException("프로필 이미지 업로드 실패", e);
            }
        }

        log.info("Persona updated: {}", personaId);

        return toResponse(persona);
    }

    /**
     * Persona 삭제 (Soft Delete)
     */
    @Transactional
    public void deletePersona(User user, Long personaId) {
        log.info("Deleting persona: {} for user: {}", personaId, user.getEmail());

        Persona persona = personaRepository.findByIdAndUserId(personaId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Persona를 찾을 수 없습니다"));

        persona.softDelete();

        log.info("Persona soft deleted: {}", personaId);
    }

    // === Helper Methods ===

    /**
     * Persona -> PersonaResponse 변환
     */
    private PersonaResponse toResponse(Persona persona) {
        return PersonaResponse.builder()
                .id(persona.getId())
                .name(encryptionUtil.decrypt(persona.getName()))
                .phoneNumber(encryptionUtil.decrypt(persona.getPhoneNumber()))
                .relationship(persona.getRelationship())
                .profileImageUrl(persona.getProfileImageUrl())
                .memo(persona.getMemo())
                .createdAt(persona.getCreatedAt())
                .updatedAt(persona.getUpdatedAt())
                .build();
    }

    /**
     * Persona -> PersonaListResponse 변환
     */
    private PersonaListResponse toListResponse(Persona persona) {
        return PersonaListResponse.builder()
                .id(persona.getId())
                .name(encryptionUtil.decrypt(persona.getName()))
                .phoneNumber(encryptionUtil.decrypt(persona.getPhoneNumber()))
                .relationship(persona.getRelationship())
                .profileImageUrl(persona.getProfileImageUrl())
                .build();
    }
}

