package com.dot.backend.controller;

import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.persona.PersonaCreateRequest;
import com.dot.backend.dto.persona.PersonaUpdateRequest;
import com.dot.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PersonaController API 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String accessToken;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("Test1234!"))
                .build();
        testUser = userRepository.save(testUser);

        // JWT 토큰 생성 (User ID 사용)
        accessToken = jwtTokenProvider.createAccessToken(testUser.getId());
    }

    // ==================== 기본 CRUD 테스트 ====================

    @Test
    @DisplayName("POST /api/personas - Persona 생성 성공")
    void createPersona_Success() throws Exception {
        // given
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name("엄마")
                .phoneNumber("010-1234-5678")
                .relationship("어머니")
                .memo("따뜻한 말투")
                .build();

        // when & then
        mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("엄마"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.relationship").value("어머니"))
                .andExpect(jsonPath("$.memo").value("따뜻한 말투"));
    }

    @Test
    @DisplayName("POST /api/personas - 잘못된 전화번호 형식으로 400 에러")
    void createPersona_InvalidPhoneNumber() throws Exception {
        // given
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name("엄마")
                .phoneNumber("01012345678") // 하이픈 없음
                .build();

        // when & then
        mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/personas - 필수 필드 누락으로 400 에러")
    void createPersona_MissingRequiredFields() throws Exception {
        // given
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                // name 누락
                .phoneNumber("010-1234-5678")
                .build();

        // when & then
        mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/personas - Persona 목록 조회 성공")
    void getPersonaList_Success() throws Exception {
        // given - 2개의 Persona 생성
        createPersonaViaApi("엄마", "010-1111-1111");
        createPersonaViaApi("아빠", "010-2222-2222");

        // when & then
        mockMvc.perform(get("/api/personas")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", containsInAnyOrder("엄마", "아빠")));
    }

    @Test
    @DisplayName("GET /api/personas/{id} - Persona 상세 조회 성공")
    void getPersonaDetail_Success() throws Exception {
        // given
        String personaId = createPersonaViaApi("엄마", "010-1234-5678");

        // when & then
        mockMvc.perform(get("/api/personas/" + personaId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personaId))
                .andExpect(jsonPath("$.name").value("엄마"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("GET /api/personas/{id} - 존재하지 않는 Persona 조회 시 404")
    void getPersonaDetail_NotFound() throws Exception {
        // when & then
        mockMvc.perform(get("/api/personas/99999")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/personas/{id} - Persona 수정 성공")
    void updatePersona_Success() throws Exception {
        // given
        String personaId = createPersonaViaApi("엄마", "010-1234-5678");

        PersonaUpdateRequest request = PersonaUpdateRequest.builder()
                .name("어머니")
                .relationship("mother")
                .memo("새로운 메모")
                .build();

        // when & then
        mockMvc.perform(put("/api/personas/" + personaId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("어머니"))
                .andExpect(jsonPath("$.relationship").value("mother"))
                .andExpect(jsonPath("$.memo").value("새로운 메모"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678")); // 전화번호 유지
    }

    @Test
    @DisplayName("DELETE /api/personas/{id} - Persona 삭제 성공")
    void deletePersona_Success() throws Exception {
        // given
        String personaId = createPersonaViaApi("엄마", "010-1234-5678");

        // when & then
        mockMvc.perform(delete("/api/personas/" + personaId)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());

        // 삭제 후 목록에서 사라짐
        mockMvc.perform(get("/api/personas")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== 인증/인가 테스트 ====================

    // Note: 인증 테스트는 Spring Security 설정에 따라 달라질 수 있음
    // 실제 환경에서는 별도의 보안 테스트 필요

    /*
    @Test
    @DisplayName("인증 없이 Persona 생성 시도 - 401")
    void createPersona_Unauthorized() throws Exception {
        // Security 설정에 따라 다를 수 있음
    }

    @Test
    @DisplayName("잘못된 토큰으로 접근 시도 - 401")
    void createPersona_InvalidToken() throws Exception {
        // Security 설정에 따라 다를 수 있음
    }
    */

    @Test
    @DisplayName("다른 사용자의 Persona 조회 시도 - 404")
    void getPersonaDetail_OtherUser() throws Exception {
        // given - 다른 사용자 생성
        User otherUser = User.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("Test1234!"))
                .build();
        otherUser = userRepository.save(otherUser);

        String otherAccessToken = jwtTokenProvider.createAccessToken(otherUser.getId());

        // testUser의 Persona 생성
        String personaId = createPersonaViaApi("엄마", "010-1234-5678");

        // when & then - otherUser로 조회 시도
        mockMvc.perform(get("/api/personas/" + personaId)
                        .header("Authorization", "Bearer " + otherAccessToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // ==================== 유효성 검증 테스트 ====================

    @Test
    @DisplayName("이름이 100자를 초과하면 400 에러")
    void createPersona_NameTooLong() throws Exception {
        // given
        String longName = "가".repeat(101);
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name(longName)
                .phoneNumber("010-1234-5678")
                .build();

        // when & then
        mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("메모가 5000자를 초과하면 400 에러")
    void createPersona_MemoTooLong() throws Exception {
        // given
        String longMemo = "가".repeat(5001);
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name("엄마")
                .phoneNumber("010-1234-5678")
                .memo(longMemo)
                .build();

        // when & then
        mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // TODO: 전화번호 중복 체크는 암호화로 인해 현재 작동하지 않음
    // 개선 필요: 모든 Persona 조회 후 복호화하여 비교하는 로직 필요
    /*
    @Test
    @DisplayName("전화번호 중복 시 400 에러")
    void createPersona_DuplicatePhoneNumber() throws Exception {
        // 암호화로 인해 현재는 작동하지 않음
    }
    */

    // ==================== Helper Methods ====================

    /**
     * API를 통해 Persona 생성하고 ID 반환
     */
    private String createPersonaViaApi(String name, String phoneNumber) throws Exception {
        PersonaCreateRequest request = PersonaCreateRequest.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .relationship("테스트관계")
                .memo("테스트메모")
                .build();

        String response = mockMvc.perform(post("/api/personas")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}





