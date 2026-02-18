package com.dot.backend.controller;

import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.dto.persona.*;
import com.dot.backend.service.PersonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * Persona 생성 (Multipart Form)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Persona 생성 (파일 업로드 포함)",
        description = "새로운 Persona를 연락처에 추가합니다.\n\n" +
            "**변경사항:**\n" +
            "- Content-Type: multipart/form-data 사용\n" +
            "- 프로필 이미지를 직접 업로드 (바이너리)\n" +
            "- 카톡 대화 파일 업로드 및 화자 선택 한 번에 처리\n\n" +
            "**필수 필드:**\n" +
            "- name: Persona 이름\n" +
            "- speakerName: 카톡 파일에서 Persona에 해당하는 화자 이름 (kakaoFile 있을 때 필수)\n\n" +
            "**선택 필드:**\n" +
            "- phoneNumber: 전화번호 (단순 추가 정보, 010-1234-5678 형식)\n" +
            "- relationship: 관계 (예: 어머니)\n" +
            "- memo: AI 참조용 메모\n" +
            "- profileImage: 프로필 이미지 파일 (JPG, PNG 등)\n" +
            "- kakaoFile: 카톡 대화 파일 (.txt)\n\n" +
            "**인증 필요:** Bearer Token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Persona 생성 성공",
            content = @Content(schema = @Schema(implementation = PersonaResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (필수 필드 누락, 파일 형식 오류, 화자 이름 불일치)",
            content = @Content(
                examples = @ExampleObject(
                    value = "{\"message\": \"화자 '엄마'를 찾을 수 없습니다\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "인증 실패"
        )
    })
    public ResponseEntity<PersonaResponse> createPersona(
        @Parameter(description = "Persona 이름", required = true)
        @RequestParam("name")
        @NotBlank(message = "이름을 입력해주세요")
        @Size(max = 100, message = "이름은 100자 이내로 입력해주세요")
        String name,

        @Parameter(description = "전화번호 (010-1234-5678 형식, 선택 사항)")
        @RequestParam(value = "phoneNumber", required = false)
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        String phoneNumber,

        @Parameter(description = "관계 (예: 어머니)")
        @RequestParam(value = "relationship", required = false)
        @Size(max = 100, message = "관계는 100자 이내로 입력해주세요")
        String relationship,

        @Parameter(description = "메모 (AI 참조용)")
        @RequestParam(value = "memo", required = false)
        @Size(max = 5000, message = "메모는 5000자 이내로 입력해주세요")
        String memo,

        @Parameter(description = "프로필 이미지 파일")
        @RequestParam(value = "profileImage", required = false)
        MultipartFile profileImage,

        @Parameter(description = "카톡 대화 파일 (.txt)")
        @RequestParam(value = "kakaoFile", required = false)
        MultipartFile kakaoFile,

        @Parameter(description = "화자 이름 (카톡 파일에서 Persona에 해당하는 이름)")
        @RequestParam(value = "speakerName", required = false)
        String speakerName,

        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);

        // 카톡 파일이 있는데 화자 이름이 없으면 에러
        if (kakaoFile != null && !kakaoFile.isEmpty()) {
            if (speakerName == null || speakerName.isBlank()) {
                throw new IllegalArgumentException("카톡 파일 업로드 시 화자 이름(speakerName)은 필수입니다");
            }
        }

        PersonaResponse response = personaService.createPersona(
                currentUser,
                name,
                phoneNumber,
                relationship,
                memo,
                profileImage,
                kakaoFile,
                speakerName
        );

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
     * Persona 수정 (Multipart Form)
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Persona 수정",
        description = "기존 Persona 정보를 수정합니다.\n\n" +
            "**수정 가능 항목:**\n" +
            "- 이름\n" +
            "- 전화번호 (선택 사항)\n" +
            "- 관계\n" +
            "- 메모\n" +
            "- 프로필 이미지 (파일 업로드)\n\n" +
            "**주의사항:**\n" +
            "- null 값은 수정하지 않음 (기존 값 유지)\n" +
            "- 본인의 Persona만 수정 가능\n" +
            "- 프로필 이미지 변경 시 기존 이미지는 S3에서 삭제\n\n" +
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

        @Parameter(description = "이름 (변경하지 않으려면 생략)")
        @RequestParam(value = "name", required = false)
        @Size(max = 100, message = "이름은 100자 이내로 입력해주세요")
        String name,

        @Parameter(description = "전화번호 (변경하지 않으려면 생략)")
        @RequestParam(value = "phoneNumber", required = false)
        @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        String phoneNumber,

        @Parameter(description = "관계 (변경하지 않으려면 생략)")
        @RequestParam(value = "relationship", required = false)
        @Size(max = 100, message = "관계는 100자 이내로 입력해주세요")
        String relationship,

        @Parameter(description = "메모 (변경하지 않으려면 생략)")
        @RequestParam(value = "memo", required = false)
        @Size(max = 5000, message = "메모는 5000자 이내로 입력해주세요")
        String memo,

        @Parameter(description = "프로필 이미지 파일 (변경하지 않으려면 생략)")
        @RequestParam(value = "profileImage", required = false)
        MultipartFile profileImage,

        @AuthenticationPrincipal UserDetails userDetails
    ) {
        User currentUser = getCurrentUser(userDetails);
        PersonaResponse response = personaService.updatePersona(
                currentUser, id, name, phoneNumber, relationship, memo, profileImage
        );

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

