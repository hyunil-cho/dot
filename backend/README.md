# Dot Backend

Dot 프로젝트 백엔드 API 서버

---

## 🚀 빠른 시작

### 개발 환경 실행
```bash
./gradlew bootRun
```

### H2 Console 접속 (개발용)
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:dotdb
Username: sa
Password: (비워두기)
```

### 빌드
```bash
./gradlew clean build
```

---

## 📚 문서

모든 개발 문서는 **[docs/](./docs/)** 디렉토리에 있습니다.

- **[docs/DATABASE_SCHEMA.md](./docs/DATABASE_SCHEMA.md)** - 데이터베이스 스키마 및 ERD
- **[docs/AI_CLIENT_GUIDE.md](./docs/AI_CLIENT_GUIDE.md)** - AI API 클라이언트 가이드
- **[docs/REPOSITORY_REFACTORING.md](./docs/REPOSITORY_REFACTORING.md)** - Repository 구조 설명
- **[docs/SCHEMA_CHANGES_SUMMARY.md](./docs/SCHEMA_CHANGES_SUMMARY.md)** - 스키마 변경 내역
- **[docs/SCHEMA_COMPLETION_REPORT.md](./docs/SCHEMA_COMPLETION_REPORT.md)** - 초기 설계 보고서

자세한 내용은 **[docs/README.md](./docs/README.md)** 참조

---

## 🏗️ 프로젝트 구조

```
backend/
├── docs/                           # 📚 개발 문서
├── src/main/
│   ├── java/com/dot/backend/
│   │   ├── client/ai/             # AI API 클라이언트
│   │   ├── domain/                # 도메인 모델
│   │   │   ├── call/              # 통화 관련
│   │   │   ├── persona/           # Persona 관리
│   │   │   ├── user/              # 사용자
│   │   │   └── voice/             # 음성 데이터
│   │   └── config/                # 설정
│   └── resources/
│       ├── application.yml        # 애플리케이션 설정
│       └── db/migration/          # Flyway DB 마이그레이션
└── src/test/                      # 테스트 코드
```

---

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA** (Hibernate)
- **Flyway** - DB 마이그레이션
- **H2** (개발), **MySQL** (프로덕션)
- **Spring Security + JWT**
- **AWS SDK** (S3)
- **Lombok**

---

## 🔧 환경 설정

### 프로파일

- **dev** (기본) - H2 메모리 DB, DevAiApiClient (Mock)
- **prod** - MySQL, AiApiClientImpl (실제 AI 연동)

### 환경 변수 (프로덕션)

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dotdb
DB_USERNAME=root
DB_PASSWORD=password

# AWS
AWS_S3_BUCKET_NAME=dot-voice-files
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key

# JWT
JWT_SECRET=your-jwt-secret-key

# Encryption
ENCRYPTION_KEY=your-aes-256-key

# AI Engine
AI_API_BASE_URL=http://ai-engine:8080/api/v1
```

---

## 📦 빌드 및 배포

### 로컬 개발
```bash
./gradlew bootRun
```

### JAR 빌드
```bash
./gradlew clean build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### 프로덕션 실행
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=mysql-server
export AI_API_BASE_URL=http://ai-engine:8080/api/v1
# ... 기타 환경 변수 설정
./gradlew bootRun
```

---

## 🧪 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 테스트
./gradlew test --tests PersonaServiceTest
```

---

## 📖 API 문서

(추후 Swagger/OpenAPI 추가 예정)

---

## 🤝 기여 가이드

1. 새 기능 개발 전 `docs/` 디렉토리의 문서 확인
2. 데이터베이스 변경 시 Flyway 마이그레이션 작성
3. Entity 변경 시 `docs/DATABASE_SCHEMA.md` 업데이트
4. 코드 스타일 준수 (Clean Architecture)

---

**문의**: Backend Team

