package com.dot.backend.domain.chatsession;

import com.dot.backend.domain.chatsession.dto.UploadResponse;
import com.dot.backend.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "ChatSession", description = "카카오톡 파일 업로드 및 채팅 세션 API")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "카톡 TXT 파일 업로드", description = "카카오톡 내보내기 TXT 파일을 업로드하고 화자 목록을 반환합니다.")
    public ResponseEntity<UploadResponse> uploadKakaoFile(
            @RequestHeader("Authorization") String bearerToken,  // 헤더에서 직접 토큰 추출
            @RequestPart("file") MultipartFile file) throws IOException {

        // "Bearer 토큰값" 에서 토큰만 추출
        String token = bearerToken.substring(7);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);  // 토큰에서 userId 추출

        UploadResponse response = chatSessionService.uploadKakaoFile(userId, file);
        return ResponseEntity.ok(response);
    }
}