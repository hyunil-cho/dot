# Backend Documentation

Dot 프로젝트 백엔드 개발 문서 모음

---

## 📚 문서 목록

### 1. [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)
**데이터베이스 스키마 문서**

- ERD (Entity Relationship Diagram)
- 테이블 상세 설명
- 인덱스 및 제약사항
- 암호화 정책
- CASCADE DELETE 정책
- AI Engine 연동 아키텍처

**주요 테이블**:
- `users` - 회원
- `user_settings` - 사용자 설정
- `personas` - 전화번호부 (Soft Delete 적용)
- `voice_data` - 음성 파일 메타데이터
- `call_sessions` - 통화 세션
- `call_logs` - 통화 기록

---

### 2. [AI_CLIENT_GUIDE.md](./AI_CLIENT_GUIDE.md)
**AI API 클라이언트 구현 가이드**

- AI API 클라이언트 인터페이스 설명
- Mock 구현체 사용법 (개발/테스트용)
- 프로덕션 구현체 가이드
- 프로파일별 동작 방식
- AI Engine 연동 시나리오

**구현체**:
- `AiApiClient` - 인터페이스
- `DevAiApiClient` - 개발용 Mock (@Profile("dev", "local"))
- `AiApiClientImpl` - 프로덕션용 (@Profile("prod"))
- `MockAiApiClient` - 테스트 전용 (src/test)

---

### 3. [REPOSITORY_REFACTORING.md](./REPOSITORY_REFACTORING.md)
**Repository 디렉토리 분리 작업 보고서**

- Repository 파일을 별도 `repository/` 디렉토리로 분리
- 변경 전/후 구조 비교
- 패키지 경로 수정 내역
- Clean Architecture 구조 준수

**변경 내용**:
```
domain/user/
├── User.java
├── UserSettings.java
└── repository/              ← 분리됨
    ├── UserRepository.java
    └── UserSettingsRepository.java
```

---

### 4. [SCHEMA_CHANGES_SUMMARY.md](./SCHEMA_CHANGES_SUMMARY.md)
**스키마 변경사항 요약**

- TRAINING_JOBS 테이블 제거 (AI API로 책임 분리)
- USERS.is_active 필드 제거
- PERSONAS에 AI Job ID 필드 추가
- VOICE_DATA.duration_seconds 제거, ai_file_id 추가
- Flyway 마이그레이션 재정렬

**주요 아키텍처 결정**:
- AI 작업 상태는 AI Engine이 관리
- REST API는 메타데이터만 캐싱
- Webhook으로 상태 동기화

---

### 5. [SCHEMA_COMPLETION_REPORT.md](./SCHEMA_COMPLETION_REPORT.md)
**스키마 작업 완료 보고서**

- 초기 스키마 설계 완료 보고
- Entity 클래스 생성 내역
- Flyway 마이그레이션 스크립트
- Repository 인터페이스
- 설계 결정 사항 정리

---

## 📖 문서 읽는 순서 (권장)

### 신규 개발자
1. **DATABASE_SCHEMA.md** - 전체 구조 파악
2. **AI_CLIENT_GUIDE.md** - AI 연동 방식 이해
3. **REPOSITORY_REFACTORING.md** - 코드 구조 이해

### 스키마 변경 이력 확인
1. **SCHEMA_COMPLETION_REPORT.md** - 초기 설계
2. **SCHEMA_CHANGES_SUMMARY.md** - 변경 내역
3. **DATABASE_SCHEMA.md** - 최신 상태

### AI 연동 개발
1. **AI_CLIENT_GUIDE.md** - 클라이언트 구현 방법
2. **DATABASE_SCHEMA.md** (AI 연동 섹션) - 데이터 흐름

---

## 🔄 문서 업데이트 정책

### 스키마 변경 시
1. Flyway 마이그레이션 작성
2. Entity 수정
3. `DATABASE_SCHEMA.md` 업데이트
4. 변경 내역을 별도 문서로 기록 (예: SCHEMA_CHANGES_v2.md)

### 아키텍처 변경 시
1. 결정 사항 문서화
2. 관련 문서 업데이트
3. 코드 수정
4. 빌드 검증

---

## 📁 프로젝트 구조

```
backend/
├── docs/                                    ← 📚 문서 디렉토리
│   ├── README.md                           (이 파일)
│   ├── DATABASE_SCHEMA.md                  (DB 스키마)
│   ├── AI_CLIENT_GUIDE.md                  (AI 클라이언트)
│   ├── REPOSITORY_REFACTORING.md           (리팩토링)
│   ├── SCHEMA_CHANGES_SUMMARY.md           (변경사항)
│   └── SCHEMA_COMPLETION_REPORT.md         (완료보고)
├── src/
│   ├── main/
│   │   ├── java/com/dot/backend/
│   │   │   ├── client/ai/                  (AI 클라이언트)
│   │   │   ├── domain/                     (도메인 모델)
│   │   │   └── config/                     (설정)
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/               (Flyway 스크립트)
│   └── test/
│       └── java/com/dot/backend/
│           └── client/ai/                  (테스트용 Mock)
├── build.gradle
└── gradlew
```

---

## 🚀 빠른 시작

### 1. 데이터베이스 스키마 확인
```bash
# DATABASE_SCHEMA.md 참조
# ERD 다이어그램 확인
# 테이블별 상세 설명 확인
```

### 2. 개발 환경 실행
```bash
# dev 프로파일로 실행 (DevAiApiClient 자동 활성화)
./gradlew bootRun

# H2 Console 접속
http://localhost:8080/h2-console
```

### 3. AI 클라이언트 사용
```java
// AI_CLIENT_GUIDE.md 참조
@Autowired
private AiApiClient aiApiClient;  // 프로파일에 따라 자동 주입

// 개발 환경: DevAiApiClient
// 프로덕션: AiApiClientImpl
```

---

## 🤝 협업 가이드

### 코드 리뷰 시
- 변경된 Entity가 있다면 → `DATABASE_SCHEMA.md` 확인
- Repository 추가/수정 시 → `repository/` 디렉토리 구조 확인
- AI 연동 변경 시 → `AI_CLIENT_GUIDE.md` 참조

### 새 기능 추가 시
1. 관련 문서 먼저 확인
2. 아키텍처 준수 여부 검토
3. 구현 후 필요 시 문서 업데이트

---

## 📞 문의

문서 관련 문의사항이 있거나 오류를 발견하셨다면, 팀 내부 채널로 공유해주세요.

---

**마지막 업데이트**: 2026-02-17
**관리자**: Backend Team

