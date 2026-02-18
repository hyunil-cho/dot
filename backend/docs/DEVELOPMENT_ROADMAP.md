# 개발 로드맵 - Persona 기반 AI 채팅 시스템

## 📋 전체 작업 흐름 개요

### 사용자 시나리오
1. **사용자**가 주소록(연락처)에 **Persona 정보**를 등록
2. **텍스트 대화 파일**을 첨부 (User와 Persona 간의 과거 대화)
3. 각 대화 내용을 **"User가 한 말"** 또는 **"Persona가 한 말"**로 구분하여 저장
4. **Persona 테이블의 memo** 필드에 추가 성향 정보 입력
5. **ChatSession 시작** 시, AI에게 "너는 이런 사람이야!" 정보 전달
6. **AI와 대화 시작** (Persona의 말투와 성격 반영)

---

## ✅ 완료된 작업 (Phase 1~2)

### 1. 인프라 및 보안 ✅
- [x] Spring Boot 프로젝트 구조 설정
- [x] MariaDB 연동 (Flyway)
- [x] JWT 기반 인증/인가 시스템
  - Access Token (15분)
  - Refresh Token (7일)
- [x] 암호화 유틸리티 (AES-256)
- [x] BCrypt 비밀번호 해싱
- [x] Spring Security 설정

### 2. 회원 관리 시스템 ✅
- [x] 회원가입 API (`POST /api/auth/signup`)
- [x] 로그인 API (`POST /api/auth/login`)
- [x] 토큰 갱신 API (`POST /api/auth/refresh`)
- [x] 회원탈퇴 API (`DELETE /api/auth/withdraw`)

### 3. 데이터베이스 스키마 ✅
- [x] `users` 테이블 - 회원 정보
- [x] `user_settings` 테이블 - 사용자 설정
- [x] `refresh_tokens` 테이블 - JWT 리프레시 토큰
- [x] `personas` 테이블 - Persona 기본 정보
- [x] `conversation_sample` 테이블 - 대화 샘플 (학습용)
- [x] `persona_trait` 테이블 - 성향 정보
- [x] `chat_message` 테이블 - 실제 채팅 메시지
- [x] `chat_sessions` 테이블 - 채팅 세션
- [x] `chat_logs` 테이블 - 채팅 기록

### 4. 문서화 ✅
- [x] DATABASE_SCHEMA.md - DB 스키마 상세
- [x] ARCHITECTURE_TEXT_BASED.md - 텍스트 기반 아키텍처
- [x] JWT_AUTH_IMPLEMENTATION.md - JWT 구현
- [x] USER_WITHDRAWAL_IMPLEMENTATION.md - 회원 탈퇴

---

## 🔄 진행 중인 작업 (Phase 3)

### 현재 단계: Persona 관리 및 대화 샘플 업로드 시스템 구현

---

## 🔜 향후 작업 계획

### Phase 3: Persona 관리 API 구현 (우선순위 ⭐⭐⭐)

#### 3-1. Persona 기본 CRUD
**목표**: 사용자가 주소록에 Persona를 등록/조회/수정/삭제

**구현 항목**:
- [ ] **PersonaService** 작성
  - [ ] `createPersona()` - Persona 생성
  - [ ] `getPersonaList()` - 사용자의 Persona 목록 조회
  - [ ] `getPersonaDetail()` - Persona 상세 조회
  - [ ] `updatePersona()` - Persona 수정 (이름, 관계, memo 등)
  - [ ] `deletePersona()` - Soft Delete (30일 유예)

- [ ] **PersonaController** 작성
  - [ ] `POST /api/personas` - Persona 등록
  - [ ] `GET /api/personas` - Persona 목록 조회
  - [ ] `GET /api/personas/{id}` - Persona 상세 조회
  - [ ] `PUT /api/personas/{id}` - Persona 수정
  - [ ] `DELETE /api/personas/{id}` - Persona 삭제

- [ ] **DTO 작성**
  - [ ] `PersonaCreateRequest` - 등록 요청
  - [ ] `PersonaUpdateRequest` - 수정 요청
  - [ ] `PersonaResponse` - 응답 DTO

---

#### 3-2. 대화 샘플 파일 업로드 및 파싱 ⭐⭐⭐
**목표**: 텍스트 파일을 업로드하여 User와 Persona의 대화를 구분하여 저장

**작업 흐름**:
```
1. 사용자가 텍스트 파일 업로드 (예: kakaotalk_export.txt)
   ↓
2. 백엔드에서 파일 파싱
   ↓
3. 각 대화 라인을 User/Persona로 구분
   ↓
4. conversation_sample 테이블에 저장
```

**구현 항목**:
- [ ] **ConversationSampleService** 작성
  - [ ] `uploadConversationFile()` - 파일 업로드 및 파싱
  - [ ] `parseTextFile()` - 텍스트 파일 파싱 로직
  - [ ] `saveConversationSamples()` - DB에 대화 샘플 저장
  - [ ] `getConversationSamples()` - Persona의 대화 샘플 조회
  - [ ] `updateSpeakerRole()` - 화자 역할 수정 (User ↔ Persona)
  - [ ] `deleteConversationSample()` - 대화 샘플 삭제

- [ ] **ConversationSampleController** 작성
  - [ ] `POST /api/personas/{id}/conversations/upload` - 파일 업로드
  - [ ] `GET /api/personas/{id}/conversations` - 대화 샘플 목록
  - [ ] `PUT /api/personas/{id}/conversations/{sampleId}` - 화자 역할 수정
  - [ ] `DELETE /api/personas/{id}/conversations/{sampleId}` - 삭제

- [ ] **DTO 작성**
  - [ ] `ConversationFileUploadRequest` - 파일 업로드 요청
  - [ ] `ConversationSampleResponse` - 대화 샘플 응답
  - [ ] `SpeakerRoleUpdateRequest` - 화자 역할 변경 요청

**파일 파싱 예시**:
```java
// 입력 텍스트 파일 예시
[2026-02-18 10:30] 엄마: 오늘 저녁 뭐 먹을래?
[2026-02-18 10:31] 나: 김치찌개 먹고 싶어요
[2026-02-18 10:32] 엄마: 알았어~ 그럼 준비할게 ㅎㅎ

// 파싱 결과
ConversationSample 1: speaker="persona", message="오늘 저녁 뭐 먹을래?", sequence=1
ConversationSample 2: speaker="user", message="김치찌개 먹고 싶어요", sequence=2
ConversationSample 3: speaker="persona", message="알았어~ 그럼 준비할게 ㅎㅎ", sequence=3
```

---

#### 3-3. Persona 성향 정보 관리
**목표**: Persona의 말투, 습관어, 성격 등을 입력/관리

**구현 항목**:
- [ ] **PersonaTraitService** 작성
  - [ ] `addTrait()` - 성향 정보 추가
  - [ ] `getTraits()` - Persona의 성향 목록 조회
  - [ ] `updateTrait()` - 성향 정보 수정
  - [ ] `deleteTrait()` - 성향 정보 삭제

- [ ] **PersonaTraitController** 작성
  - [ ] `POST /api/personas/{id}/traits` - 성향 추가
  - [ ] `GET /api/personas/{id}/traits` - 성향 목록
  - [ ] `PUT /api/personas/{id}/traits/{traitId}` - 성향 수정
  - [ ] `DELETE /api/personas/{id}/traits/{traitId}` - 성향 삭제

- [ ] **DTO 작성**
  - [ ] `PersonaTraitRequest` - 성향 추가/수정 요청
  - [ ] `PersonaTraitResponse` - 성향 응답

**성향 정보 예시**:
```json
{
  "traitType": "speech_pattern",
  "traitValue": "~인 것 같아, ~하는 편이야"
},
{
  "traitType": "habit_word",
  "traitValue": "ㅎㅎ, ㅋㅋ"
},
{
  "traitType": "personality",
  "traitValue": "밝고 긍정적, 가족을 많이 생각함"
}
```

---

### Phase 4: AI 채팅 시스템 구현 (우선순위 ⭐⭐⭐)

#### 4-1. AI 추상화 레이어
**목표**: LLM API를 교체 가능하게 인터페이스로 추상화

**구현 항목**:
- [ ] **ChatAiService 인터페이스** 작성
  ```java
  String generateResponse(
      Long personaId,
      String systemPrompt,
      List<ChatMessageDto> conversationHistory,
      String userMessage
  );
  ```

- [ ] **MockChatAiService** 구현 (개발/테스트용)
  - [ ] 간단한 규칙 기반 응답
  - [ ] systemPrompt에서 키워드 추출
  - [ ] 랜덤 응답 생성

- [ ] **application.yml** 설정
  ```yaml
  ai:
    service:
      type: mock  # dev 환경
  ```

---

#### 4-2. 시스템 프롬프트 생성 로직
**목표**: ChatSession 시작 시 "AI야 너는 이런 사람이야!" 정보 생성

**구현 항목**:
- [ ] **PromptBuilder** 유틸리티 작성
  - [ ] `buildSystemPrompt()` - 시스템 프롬프트 생성
  - [ ] Persona의 memo, PersonaTrait, ConversationSample 활용

**시스템 프롬프트 예시**:
```
너는 사용자의 어머니 역할을 합니다.

[기본 정보]
- 이름: 김영희
- 관계: 어머니
- 메모: 항상 따뜻하고 자식 걱정을 많이 하심

[말투 특징]
- ~인 것 같아, ~하는 편이야
- 자주 사용하는 표현: ㅎㅎ, ㅋㅋ

[성격]
- 밝고 긍정적
- 가족을 많이 생각함
- 요리를 좋아함

[대화 예시]
사용자: 오늘 뭐 먹을까요?
어머니: 김치찌개 어때? 엄마가 해줄게~ ㅎㅎ

사용자: 저녁 늦게 들어가요
어머니: 조심히 들어와~ 배고프면 냉장고에 반찬 있어!

이제 사용자와 자연스럽게 대화해주세요.
```

---

#### 4-3. ChatService 구현
**목표**: 채팅 메시지 송수신 및 AI 응답 생성

**구현 항목**:
- [ ] **ChatService** 작성
  - [ ] `startChatSession()` - 채팅 세션 시작
  - [ ] `sendMessage()` - 메시지 전송 및 AI 응답
  - [ ] `getChatHistory()` - 대화 기록 조회
  - [ ] `endChatSession()` - 채팅 세션 종료

**메시지 전송 흐름**:
```
1. 사용자가 메시지 전송
   ↓
2. ChatMessage 저장 (sender_type=user)
   ↓
3. Persona 정보 조회
   ↓
4. 시스템 프롬프트 생성 (PromptBuilder)
   ↓
5. 최근 N개 ChatMessage 조회 (컨텍스트)
   ↓
6. ChatAiService.generateResponse() 호출
   ↓
7. AI 응답 받음
   ↓
8. ChatMessage 저장 (sender_type=assistant)
   ↓
9. 사용자에게 응답 반환
```

- [ ] **ChatController** 작성
  - [ ] `POST /api/chat/{personaId}/start` - 채팅 시작
  - [ ] `POST /api/chat/{personaId}/messages` - 메시지 전송
  - [ ] `GET /api/chat/{personaId}/history` - 대화 기록
  - [ ] `POST /api/chat/{personaId}/end` - 채팅 종료

- [ ] **DTO 작성**
  - [ ] `ChatStartRequest` - 채팅 시작 요청
  - [ ] `ChatMessageRequest` - 메시지 전송 요청
  - [ ] `ChatMessageResponse` - AI 응답
  - [ ] `ChatHistoryResponse` - 대화 기록 응답

---

### Phase 5: 프론트엔드 연동 및 테스트 (우선순위 ⭐⭐)

**구현 항목**:
- [ ] Swagger UI 문서 작성
- [ ] Postman 테스트 컬렉션 작성
- [ ] 프론트엔드 팀에 API 명세 전달
- [ ] 통합 테스트
  - [ ] Persona 등록 → 대화 파일 업로드 → 채팅 시작 → 메시지 송수신

---

### Phase 6: AI 팀 협업 (AI 팀 합류 후)

**구현 항목**:
- [ ] LLM API 선택 (OpenAI, Claude 등)
- [ ] OpenAiService 또는 ClaudeService 구현
- [ ] API Key 관리 (환경변수)
- [ ] Prompt Engineering 최적화
- [ ] 응답 품질 테스트 및 개선

---

### Phase 7: EC2 배포 및 운영 (우선순위 ⭐)

**구현 항목**:
- [ ] Dockerfile 작성
- [ ] docker-compose.yml 작성
- [ ] deploy.sh 배포 스크립트
- [ ] 환경변수 설정 (.env)
- [ ] HTTPS 설정 (Let's Encrypt)
- [ ] 모니터링 설정 (로그, 메트릭)

---

## 📊 작업 우선순위 요약

| Phase | 작업 | 우선순위 | 예상 시간 | 상태 |
|:---:|:---|:---:|:---:|:---:|
| 1-2 | 회원 관리 & 스키마 | ⭐⭐⭐ | - | ✅ 완료 |
| 3-1 | Persona 기본 CRUD | ⭐⭐⭐ | 2h | 🔜 다음 |
| 3-2 | 대화 파일 업로드/파싱 | ⭐⭐⭐ | 3h | 🔜 다음 |
| 3-3 | 성향 정보 관리 | ⭐⭐⭐ | 2h | 🔜 다음 |
| 4-1 | AI 추상화 레이어 | ⭐⭐⭐ | 1.5h | 🔜 대기 |
| 4-2 | 시스템 프롬프트 생성 | ⭐⭐⭐ | 1h | 🔜 대기 |
| 4-3 | ChatService 구현 | ⭐⭐⭐ | 3h | 🔜 대기 |
| 5 | 프론트엔드 연동 | ⭐⭐ | 2h | 🔜 대기 |
| 6 | AI 팀 협업 | ⭐ | TBD | ⏸️ 보류 |
| 7 | EC2 배포 | ⭐ | 3h | ⏸️ 보류 |

---

## 🎯 즉시 시작 가능한 다음 작업

### 1단계: PersonaService & PersonaController
```
✅ 우선 구현: Persona 기본 CRUD
- PersonaService.java
- PersonaController.java
- PersonaCreateRequest.java
- PersonaResponse.java
```

### 2단계: ConversationSampleService
```
✅ 우선 구현: 대화 파일 업로드 및 파싱
- ConversationSampleService.java
- ConversationSampleController.java
- 파일 파싱 로직
- 화자 역할 구분 UI 지원
```

### 3단계: ChatService + AI 추상화
```
✅ 우선 구현: 채팅 시스템
- ChatAiService (인터페이스)
- MockChatAiService (Mock)
- PromptBuilder
- ChatService.java
- ChatController.java
```

---

## 🔍 핵심 기술 요구사항

### 파일 업로드 처리
- **MultipartFile** 사용
- 텍스트 파싱 (정규표현식 또는 라인별 처리)
- 화자 구분 로직

### 시스템 프롬프트 생성
- **Persona.memo** 활용
- **PersonaTrait** 목록 조합
- **ConversationSample** Few-shot 예시 포함

### AI 응답 생성
- **대화 컨텍스트** 관리 (최근 N개 메시지)
- **토큰 제한** 고려 (LLM 모델에 따라 다름)
- **비동기 처리** (응답 대기 시간 최소화)

---

## 📝 작업 시작 확인

**다음 작업을 시작하시겠습니까?**

1. ✅ **PersonaService 구현** (Persona 기본 CRUD)
2. ✅ **ConversationSampleService 구현** (대화 파일 업로드)
3. ✅ **ChatService 구현** (채팅 시스템)

각 단계별로 순차적으로 진행하거나, 병렬 작업도 가능합니다.