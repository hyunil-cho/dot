package com.dot.backend.domain.chatsession;

import com.dot.backend.domain.chatsession.dto.UploadResponse;
import com.dot.backend.domain.chatsession.repository.ChatSessionRepository;
import com.dot.backend.domain.persona.Persona;
import com.dot.backend.domain.persona.repository.PersonaRepository;
import com.dot.backend.domain.user.User;
import com.dot.backend.domain.user.repository.UserRepository;
import com.dot.backend.parser.KakaoTxtParser;
import com.dot.backend.parser.ParsedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final UserRepository userRepository;
    private final KakaoTxtParser kakaoTxtParser;

    private static final long MAX_FILE_SIZE = 1024 * 1024; // 1MB

    /**
     * 1단계: 카톡 TXT 파일 업로드 → 화자 목록 반환
     */
    @Transactional
    public UploadResponse uploadKakaoFile(Long userId, MultipartFile file) throws IOException {

        // 파일 유효성 검사
        validateFile(file);

        // 파일 파싱
        List<ParsedMessage> messages = kakaoTxtParser.parse(file.getInputStream());

        if (messages.isEmpty()) {
            throw new IllegalArgumentException("파싱된 메시지가 없습니다. 올바른 카카오톡 내보내기 파일인지 확인해주세요.");
        }

        // 화자 목록 추출
        List<String> speakers = kakaoTxtParser.extractSpeakers(messages);

        // User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 임시 Persona 없이 세션 생성 (persona_id가 NOT NULL이라 임시 처리)
        // 화자 선택 후 persona와 연결할 예정
        ChatSession session = ChatSession.builder()
                .user(user)
                .originalFileName(file.getOriginalFilename())
                .build();

        // 대화 샘플을 세션에 임시 저장 (나중에 화자 선택 시 활용)
        // 지금은 파싱 결과를 메모리에 들고 있다가 화자 선택 API에서 씀
        // → 실제로는 conversation_sample 테이블에 저장해야 함 (후에서 구현)

        chatSessionRepository.save(session);

        return new UploadResponse(session.getId(), speakers);
    }

    /**
     * 파일 유효성 검사
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 1MB를 초과할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.endsWith(".txt")) {
            throw new IllegalArgumentException("txt 파일만 업로드 가능합니다.");
        }
    }
}
