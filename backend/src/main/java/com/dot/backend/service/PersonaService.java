package com.dot.backend.service;

import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.dto.persona.*;
import com.dot.backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Persona 생성
     */
    @Transactional
    public PersonaResponse createPersona(User user, PersonaCreateRequest request) {
        log.info("Creating persona for user: {}", user.getEmail());

        // 전화번호 암호화
        String encryptedPhoneNumber = encryptionUtil.encrypt(request.getPhoneNumber());

        // 중복 확인
        if (personaRepository.existsByUserIdAndPhoneNumberAndIsDeletedFalse(user.getId(), encryptedPhoneNumber)) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다");
        }

        // 이름 암호화
        String encryptedName = encryptionUtil.encrypt(request.getName());

        // Persona 생성
        Persona persona = Persona.builder()
                .user(user)
                .name(encryptedName)
                .phoneNumber(encryptedPhoneNumber)
                .relationship(request.getRelationship())
                .memo(request.getMemo())
                .profileImageUrl(request.getProfileImageUrl())
                .isDeleted(false)
                .build();

        Persona savedPersona = personaRepository.save(persona);

        log.info("Persona created with ID: {}", savedPersona.getId());

        return toResponse(savedPersona);
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
     * Persona 수정
     */
    @Transactional
    public PersonaResponse updatePersona(User user, Long personaId, PersonaUpdateRequest request) {
        log.info("Updating persona: {} for user: {}", personaId, user.getEmail());

        Persona persona = personaRepository.findByIdAndUserId(personaId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Persona를 찾을 수 없습니다"));

        // 이름 변경 시 암호화
        String encryptedName = null;
        if (request.getName() != null && !request.getName().isBlank()) {
            encryptedName = encryptionUtil.encrypt(request.getName());
        }

        // 프로필 업데이트
        persona.updateProfile(encryptedName, request.getRelationship(), request.getMemo());

        // 프로필 이미지 업데이트
        if (request.getProfileImageUrl() != null) {
            persona.updateProfileImage(request.getProfileImageUrl());
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

