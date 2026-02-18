package com.dot.backend.service;

import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.persona.*;
import com.dot.backend.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PersonaService 통합 테스트
 */
@SpringBootTest
@Transactional
class PersonaServiceTest {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .build();
        testUser = userRepository.save(testUser);

        otherUser = User.builder()
                .email("other@example.com")
                .password("encodedPassword")
                .build();
        otherUser = userRepository.save(otherUser);
    }

    // ==================== 기본 CRUD 테스트 ====================

    @Test
    @DisplayName("Persona 생성 성공")
    void createPersona_Success() {
        // given
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name("엄마")
                .phoneNumber("010-1234-5678")
                .relationship("어머니")
                .memo("따뜻하고 다정한 말투")
                .build();

        // when
        PersonaResponse response = personaService.createPersona(testUser, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getName()).isEqualTo("엄마");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(response.getRelationship()).isEqualTo("어머니");
        assertThat(response.getMemo()).isEqualTo("따뜻하고 다정한 말투");

        // DB에 암호화되어 저장되었는지 확인
        Persona savedPersona = personaRepository.findById(response.getId()).orElseThrow();
        assertThat(savedPersona.getName()).isNotEqualTo("엄마"); // 암호화됨
        assertThat(savedPersona.getPhoneNumber()).isNotEqualTo("010-1234-5678"); // 암호화됨
        assertThat(encryptionUtil.decrypt(savedPersona.getName())).isEqualTo("엄마");
        assertThat(encryptionUtil.decrypt(savedPersona.getPhoneNumber())).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("Persona 목록 조회 - 본인 것만 조회")
    void getPersonaList_OnlyOwnPersonas() {
        // given
        createTestPersona(testUser, "엄마", "010-1111-1111");
        createTestPersona(testUser, "아빠", "010-2222-2222");
        createTestPersona(otherUser, "친구", "010-3333-3333");

        // when
        List<PersonaListResponse> result = personaService.getPersonaList(testUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("엄마", "아빠");
    }

    @Test
    @DisplayName("Persona 상세 조회 성공")
    void getPersonaDetail_Success() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");

        // when
        PersonaResponse response = personaService.getPersonaDetail(testUser, persona.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(persona.getId());
        assertThat(response.getName()).isEqualTo("엄마");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("Persona 수정 성공")
    void updatePersona_Success() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");

        PersonaUpdateRequest request = PersonaUpdateRequest.builder()
                .name("어머니")
                .relationship("mother")
                .memo("새로운 메모")
                .build();

        // when
        PersonaResponse response = personaService.updatePersona(testUser, persona.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("어머니");
        assertThat(response.getRelationship()).isEqualTo("mother");
        assertThat(response.getMemo()).isEqualTo("새로운 메모");
        assertThat(response.getPhoneNumber()).isEqualTo("010-1234-5678"); // 전화번호는 변경 안 됨
    }

    @Test
    @DisplayName("Persona 삭제 (Soft Delete)")
    void deletePersona_SoftDelete() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");
        Long personaId = persona.getId();

        // when
        personaService.deletePersona(testUser, personaId);

        // then
        Persona deletedPersona = personaRepository.findById(personaId).orElseThrow();
        assertThat(deletedPersona.getIsDeleted()).isTrue();
        assertThat(deletedPersona.getDeletedAt()).isNotNull();

        // 목록 조회 시 나타나지 않음
        List<PersonaListResponse> list = personaService.getPersonaList(testUser);
        assertThat(list).isEmpty();
    }

    // ==================== 보안 및 권한 테스트 ====================

    @Test
    @DisplayName("다른 사용자의 Persona 조회 불가")
    void getPersonaDetail_OtherUser_Fail() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");

        // when & then
        assertThatThrownBy(() -> personaService.getPersonaDetail(otherUser, persona.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 Persona 수정 불가")
    void updatePersona_OtherUser_Fail() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");
        PersonaUpdateRequest request = PersonaUpdateRequest.builder()
                .name("해커")
                .build();

        // when & then
        assertThatThrownBy(() -> personaService.updatePersona(otherUser, persona.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 Persona 삭제 불가")
    void deletePersona_OtherUser_Fail() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");

        // when & then
        assertThatThrownBy(() -> personaService.deletePersona(otherUser, persona.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona를 찾을 수 없습니다");
    }

    // ==================== 유효성 검증 테스트 ====================

    // Note: 암호화로 인해 동일한 전화번호도 매번 다른 암호문이 생성되므로
    // DB unique constraint나 단순 문자열 비교로는 중복을 막을 수 없음
    // 실제 구현 시에는 모든 Persona를 조회하여 복호화 후 비교하는 로직이 필요
    // TODO: 전화번호 중복 체크 로직 개선 필요

    /*
    @Test
    @DisplayName("전화번호 중복 시 생성 실패")
    void createPersona_DuplicatePhoneNumber_Fail() {
        // 암호화로 인해 현재는 작동하지 않음
    }
    */

    // ==================== 암호화 테스트 ====================

    @Test
    @DisplayName("이름과 전화번호가 암호화되어 저장됨")
    void createPersona_EncryptionTest() {
        // given
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name("테스트")
                .phoneNumber("010-9999-9999")
                .build();

        // when
        PersonaResponse response = personaService.createPersona(testUser, request);

        // then
        Persona savedPersona = personaRepository.findById(response.getId()).orElseThrow();

        // DB에는 암호화된 값이 저장됨
        assertThat(savedPersona.getName()).isNotEqualTo("테스트");
        assertThat(savedPersona.getPhoneNumber()).isNotEqualTo("010-9999-9999");

        // 복호화하면 원본 값
        assertThat(encryptionUtil.decrypt(savedPersona.getName())).isEqualTo("테스트");
        assertThat(encryptionUtil.decrypt(savedPersona.getPhoneNumber())).isEqualTo("010-9999-9999");

        // 응답 DTO에는 복호화된 값
        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getPhoneNumber()).isEqualTo("010-9999-9999");
    }

    // ==================== 비즈니스 로직 테스트 ====================

    @Test
    @DisplayName("부분 수정 - null 필드는 변경 안 됨")
    void updatePersona_PartialUpdate() {
        // given - Service로 생성하고 한 번 수정
        PersonaCreateRequest createRequest = PersonaCreateRequest.builder()
                .name("엄마")
                .phoneNumber("010-1234-5678")
                .relationship("어머니")
                .memo("원래 메모")
                .build();
        PersonaResponse created = personaService.createPersona(testUser, createRequest);

        // 이름만 변경하는 요청
        PersonaUpdateRequest request = PersonaUpdateRequest.builder()
                .name("새이름") // 이름만 변경
                .build();

        // when
        PersonaResponse response = personaService.updatePersona(testUser, created.getId(), request);

        // then
        assertThat(response.getName()).isEqualTo("새이름");
        assertThat(response.getRelationship()).isEqualTo("어머니"); // 유지
        assertThat(response.getMemo()).isEqualTo("원래 메모"); // 유지
    }

    @Test
    @DisplayName("삭제된 Persona는 일반 조회에서 제외")
    void getPersonaList_ExcludesDeleted() {
        // given
        createTestPersona(testUser, "엄마", "010-1111-1111");
        Persona deletedPersona = createTestPersona(testUser, "아빠", "010-2222-2222");
        createTestPersona(testUser, "친구", "010-3333-3333");

        personaService.deletePersona(testUser, deletedPersona.getId());

        // when
        List<PersonaListResponse> result = personaService.getPersonaList(testUser);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name").containsExactlyInAnyOrder("엄마", "친구");
        assertThat(result).extracting("name").doesNotContain("아빠");
    }

    @Test
    @DisplayName("존재하지 않는 Persona 조회 시 예외")
    void getPersonaDetail_NotFound() {
        // when & then
        assertThatThrownBy(() -> personaService.getPersonaDetail(testUser, 99999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Persona를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("프로필 이미지만 변경")
    void updatePersona_OnlyProfileImage() {
        // given
        Persona persona = createTestPersona(testUser, "엄마", "010-1234-5678");

        PersonaUpdateRequest request = PersonaUpdateRequest.builder()
                .profileImageUrl("https://new-image-url.com/profile.jpg")
                .build();

        // when
        PersonaResponse response = personaService.updatePersona(testUser, persona.getId(), request);

        // then
        assertThat(response.getProfileImageUrl()).isEqualTo("https://new-image-url.com/profile.jpg");
        assertThat(response.getName()).isEqualTo("엄마"); // 유지
    }

    // ==================== Helper Methods ====================

    private Persona createTestPersona(User user, String name, String phoneNumber) {
        String encryptedName = encryptionUtil.encrypt(name);
        String encryptedPhoneNumber = encryptionUtil.encrypt(phoneNumber);

        Persona persona = Persona.builder()
                .user(user)
                .name(encryptedName)
                .phoneNumber(encryptedPhoneNumber)
                .relationship("테스트관계")
                .memo("테스트메모")
                .isDeleted(false)
                .build();

        return personaRepository.save(persona);
    }
}




