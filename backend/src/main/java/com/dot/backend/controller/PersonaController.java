package com.dot.backend.controller;

import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.persona.*;
import com.dot.backend.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Persona 관리 API 컨트롤러
 *
 * - POST /api/personas - Persona 생성
 * - GET /api/personas - Persona 목록 조회
 * - GET /api/personas/{id} - Persona 상세 조회
 * - PUT /api/personas/{id} - Persona 수정
 * - DELETE /api/personas/{id} - Persona 삭제
 */
@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
@Tag(name = "Persona", description = "Persona(연락처) 관리 API")
@SecurityRequirement(name = "bearerAuth")
public class PersonaController {

    private final PersonaService personaService;
    private final UserRepository userRepository;

    /**
     * Persona 생성
     */
    @PostMapping
    @Operation(
        summary = "Persona 생성",
        description = "새로운 Persona를 연락처에 추가합니다.\n\n" +
            "**주요 정보:**\n" +
            "- 전화번호는 고유 식별자로 사용 (중복 불가)\n" +
            "- 이름과 전화번호는 암호화되어 저장\n" +
            "- memo 필드는 AI가 대화 생성 시 참조\n\n" +
            "**전화번호 형식:**\n" +
            "- 010-1234-5678 형식 사용\n" +
            "- 하이픈(-) 필수\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Persona 생성 성공",
            content = @Content(
                schema = @Schema(implementation = PersonaResponse.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"id\": 1,\n" +
                        "  \"name\": \"엄마\",\n" +
                        "  \"phoneNumber\": \"010-1234-5678\",\n" +
                        "  \"relationship\": \"어머니\",\n" +
                        "  \"profileImageUrl\": null,\n" +
                        "  \"memo\": \"따뜻하고 다정한 말투\",\n" +
                        "  \"createdAt\": \"2026-02-18T10:30:00\",\n" +
                        "  \"updatedAt\": \"2026-02-18T10:30:00\"\n" +
                        "}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (전화번호 형식 오류, 중복된 전화번호)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"이미 등록된 전화번호입니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"인증이 필요합니다\"}"
                )
            )
        )
    })
    public ResponseEntity<PersonaResponse> createPersona(
        @Valid @RequestBody PersonaCreateRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        PersonaResponse response = personaService.createPersona(currentUser, request);

        return ResponseEntity
                .created(URI.create("/api/personas/" + response.getId()))
                .body(response);
    }

    /**
     * Persona 목록 조회
     */
    @GetMapping
    @Operation(
        summary = "Persona 목록 조회",
        description = "현재 로그인한 사용자의 모든 Persona를 조회합니다.\n\n" +
            "**특징:**\n" +
            "- 삭제되지 않은 활성 Persona만 조회\n" +
            "- 목록용 간소화된 정보 제공\n" +
            "- 이름과 전화번호는 복호화되어 반환\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(
                schema = @Schema(implementation = PersonaListResponse.class),
                examples = @ExampleObject(
                    value = "[\n" +
                        "  {\n" +
                        "    \"id\": 1,\n" +
                        "    \"name\": \"엄마\",\n" +
                        "    \"phoneNumber\": \"010-1234-5678\",\n" +
                        "    \"relationship\": \"어머니\",\n" +
                        "    \"profileImageUrl\": null\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"id\": 2,\n" +
                        "    \"name\": \"아빠\",\n" +
                        "    \"phoneNumber\": \"010-9876-5432\",\n" +
                        "    \"relationship\": \"아버지\",\n" +
                        "    \"profileImageUrl\": \"https://s3.../profile.jpg\"\n" +
                        "  }\n" +
                        "]"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<List<PersonaListResponse>> getPersonaList(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        List<PersonaListResponse> response = personaService.getPersonaList(currentUser);

        return ResponseEntity.ok(response);
    }

    /**
     * Persona 상세 조회
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Persona 상세 조회",
        description = "특정 Persona의 상세 정보를 조회합니다.\n\n" +
            "**특징:**\n" +
            "- 본인의 Persona만 조회 가능\n" +
            "- memo 필드 포함 (AI 참조용 정보)\n" +
            "- 생성/수정 시간 정보 제공\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "조회 성공",
            content = @Content(schema = @Schema(implementation = PersonaResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Persona를 찾을 수 없음",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"Persona를 찾을 수 없습니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<PersonaResponse> getPersonaDetail(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        PersonaResponse response = personaService.getPersonaDetail(currentUser, id);

        return ResponseEntity.ok(response);
    }

    /**
     * Persona 수정
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Persona 수정",
        description = "기존 Persona 정보를 수정합니다.\n\n" +
            "**수정 가능 항목:**\n" +
            "- 이름\n" +
            "- 관계\n" +
            "- 메모\n" +
            "- 프로필 이미지 URL\n\n" +
            "**주의사항:**\n" +
            "- 전화번호는 수정 불가 (식별자로 사용)\n" +
            "- null 값은 수정하지 않음\n" +
            "- 본인의 Persona만 수정 가능\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "수정 성공",
            content = @Content(schema = @Schema(implementation = PersonaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Persona를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<PersonaResponse> updatePersona(
        @PathVariable Long id,
        @Valid @RequestBody PersonaUpdateRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        PersonaResponse response = personaService.updatePersona(currentUser, id, request);

        return ResponseEntity.ok(response);
    }

    /**
     * Persona 삭제 (Soft Delete)
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Persona 삭제",
        description = "Persona를 삭제합니다 (Soft Delete).\n\n" +
            "**처리 과정:**\n" +
            "1. Persona를 즉시 삭제하지 않고 '삭제됨' 상태로 표시\n" +
            "2. 30일간 보관 후 완전 삭제 (복원 가능)\n" +
            "3. 관련 데이터(대화 기록, 학습 데이터 등)도 함께 삭제\n\n" +
            "**주의사항:**\n" +
            "- 삭제 후 30일 이내 복원 가능\n" +
            "- 본인의 Persona만 삭제 가능\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "삭제 성공 (No Content)"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Persona를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<Void> deletePersona(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        personaService.deletePersona(currentUser, id);

        return ResponseEntity.noContent().build();
    }

    // === Helper Methods ===

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

