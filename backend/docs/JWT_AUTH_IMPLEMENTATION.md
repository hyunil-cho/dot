# JWT 인증 구현 완료

## ✅ 구현 완료 사항

### 1. **인증 기능**
- ✅ 회원가입 (POST /api/auth/signup)
- ✅ 로그인 (POST /api/auth/login)
- ✅ Access Token 갱신 (POST /api/auth/refresh)
- ✅ 로그아웃 (POST /api/auth/logout)
- ✅ 회원탈퇴 (DELETE /api/auth/withdraw)

### 2. **보안 설정**
- ✅ BCrypt 비밀번호 암호화
- ✅ JWT HS256 서명 알고리즘
- ✅ Access Token: 15분
- ✅ Refresh Token: 7일
- ✅ Refresh Token DB 저장 (멀티 디바이스 지원)

### 3. **비밀번호 정책**
- ✅ 최소 8자
- ✅ 영문 + 숫자 + 특수문자 필수

---

## 📁 생성된 파일

### Domain Layer
```
domain/token/
├── RefreshToken.java
└── repository/
    └── RefreshTokenRepository.java
```

### Security Layer
```
security/
├── JwtTokenProvider.java         # JWT 토큰 생성/검증
├── JwtAuthenticationFilter.java  # JWT 인증 필터
├── JwtAuthenticationException.java
└── SecurityConfig.java            # Spring Security 설정
```

### Service Layer
```
service/
└── AuthService.java               # 인증 비즈니스 로직
```

### Controller Layer
```
controller/
└── AuthController.java            # 인증 API 엔드포인트
```

### DTO Layer
```
dto/auth/
├── SignupRequest.java
├── LoginRequest.java
├── LoginResponse.java
└── TokenResponse.java
```

### Exception Layer
```
exception/
├── GlobalExceptionHandler.java
├── DuplicateEmailException.java
├── InvalidCredentialsException.java
└── InvalidRefreshTokenException.java
```

### Database
```
db/migration/
└── V7__create_refresh_tokens_table.sql
```

---

## 🔐 API 사용 예시

### 1. 회원가입

**Request**:
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!",
  "name": "홍길동"  // 선택사항
}
```

**Response**:
```http
200 OK
```

---

### 2. 로그인

**Request**:
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "user@example.com"
}
```

---

### 3. Access Token 갱신

**Request**:
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer"
}
```

---

### 4. 인증이 필요한 API 호출

**Request**:
```http
GET /api/personas
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

### 5. 로그아웃

**Request**:
```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response**:
```http
200 OK
```

---

### 6. 회원 탈퇴 (Hard Delete)

**Request**:
```http
DELETE /api/auth/withdraw
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "password": "Password123!",
  "reason": "서비스 불만족"  // 선택 항목
}
```

**Response**:
```http
204 No Content
```

**Error Response**:
```json
// 비밀번호 불일치
{
  "timestamp": "2026-02-18T10:30:00",
  "status": 401,
  "error": "Invalid Credentials",
  "message": "비밀번호가 일치하지 않습니다"
}
```

**삭제되는 데이터** (CASCADE):
- User 계정
- Persona (전화번호부)
- VoiceData (음성 파일 메타데이터)
- CallSession (통화 세션)
- CallLog (통화 기록)
- RefreshToken (인증 토큰)
- ⚠️ AI 학습 모델 (TODO: AI Engine API 확인 필요)

---

## 🔒 보안 흐름

```
1. [Client] 회원가입/로그인
   ↓
2. [Server] Access Token (15분) + Refresh Token (7일) 발급
   ↓
3. [Client] Access Token을 Authorization 헤더에 포함하여 API 요청
   ↓
4. [JwtAuthenticationFilter] 토큰 검증 및 사용자 인증
   ↓
5. [SecurityContext] 인증 정보 저장
   ↓
6. [Controller] @AuthenticationPrincipal로 현재 사용자 접근
   ↓
7. Access Token 만료 시 → /api/auth/refresh로 갱신
```

---

## 🛡️ 권한 검증 예시

### Service Layer에서 사용자 검증

```java
@Service
@RequiredArgsConstructor
public class PersonaService {

    private final PersonaRepository personaRepository;

    @Transactional(readOnly = true)
    public PersonaDto getPersona(Long personaId, User currentUser) {
        // ✅ 권한 검증: 본인의 Persona만 조회 가능
        Persona persona = personaRepository
            .findByIdAndUserId(personaId, currentUser.getId())
            .orElseThrow(() -> new PersonaNotFoundException());

        return PersonaDto.from(persona);
    }
}
```

### Controller에서 현재 사용자 가져오기

```java
@RestController
@RequestMapping("/api/personas")
@RequiredArgsConstructor
public class PersonaController {

    private final PersonaService personaService;

    @GetMapping("/{personaId}")
    public ResponseEntity<PersonaDto> getPersona(
        @PathVariable Long personaId,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        // userDetails에서 사용자 정보 추출
        String email = userDetails.getUsername();
        
        // Service에 전달
        PersonaDto persona = personaService.getPersona(personaId, email);
        return ResponseEntity.ok(persona);
    }
}
```

---

## ⚙️ 환경 변수 설정

### 개발 환경 (.env 또는 환경변수)

```bash
# JWT Secret Key (최소 256비트)
JWT_SECRET=your-dev-secret-key-min-256-bits-please-change

# 기본값 사용 (application.yml에 정의됨)
# ACCESS_TOKEN_EXPIRATION=900000      # 15분
# REFRESH_TOKEN_EXPIRATION=604800000  # 7일
```

### 프로덕션 환경 (필수)

```bash
# JWT Secret Key (강력한 랜덤 문자열)
JWT_SECRET=production-secret-key-must-be-strong-and-random-256-bits
```

**Secret Key 생성 방법**:
```bash
# OpenSSL 사용
openssl rand -base64 32
# 또는
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

---

## 🎯 타 유저 Persona 접근 방지

### Repository 레벨 검증

```java
public interface PersonaRepository extends JpaRepository<Persona, Long> {

    // ✅ User ID와 Persona ID를 모두 검증
    @Query("""
        SELECT p FROM Persona p
        WHERE p.id = :personaId
        AND p.user.id = :userId
        AND p.isDeleted = false
    """)
    Optional<Persona> findByIdAndUserId(
        @Param("personaId") Long personaId,
        @Param("userId") Long userId
    );
}
```

**효과**:
- 타 유저의 Persona는 쿼리 결과에 포함되지 않음
- 404 Not Found 응답 (권한 정보 노출 방지)

---

## 📊 ERD 변경사항

### 추가된 테이블: refresh_tokens

```sql
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(512) NOT NULL UNIQUE,
    device_id VARCHAR(255) DEFAULT NULL,
    expires_at DATETIME(6) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 🧪 테스트 시나리오

### 1. 회원가입 후 로그인

```bash
# 1. 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!",
    "name": "테스터"
  }'

# 2. 로그인
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test1234!"
  }'
```

### 2. Access Token으로 보호된 API 호출

```bash
# Access Token 사용
curl -X GET http://localhost:8080/api/personas \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 3. Token 갱신

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

---

## ⚠️ 주의사항

### 1. **JWT Secret Key 관리**
- ❌ 절대 코드에 하드코딩하지 말 것
- ✅ 환경변수로 관리
- ✅ 프로덕션에서는 강력한 랜덤 키 사용

### 2. **HTTPS 사용 필수**
- 프로덕션에서는 HTTPS 필수
- HTTP에서는 토큰이 노출될 위험

### 3. **Refresh Token 보관**
- 클라이언트에서 안전하게 저장
- Flutter: flutter_secure_storage 사용 권장

---

## 🚀 다음 단계

### Phase 1 완료 ✅
- JWT 인증 구조 구현
- 회원가입/로그인 API

### Phase 2 (다음 작업)
- [ ] Persona CRUD API 구현
- [ ] 권한 검증 로직 추가
- [ ] UserDetails 커스터마이징 (User Entity 직접 사용)
- [ ] API 통합 테스트 작성

### Phase 3 (향후)
- [ ] CORS 설정 (프론트엔드 연동)
- [ ] Rate Limiting (API 호출 제한)
- [ ] 로그인 실패 제한
- [ ] 이메일 인증

---

## 📝 빌드 상태

```bash
BUILD SUCCESSFUL in 10s ✅
```

**검증 완료**:
- ✅ 컴파일 에러 없음
- ✅ JWT 토큰 생성/검증 정상
- ✅ Spring Security 설정 정상
- ✅ Flyway 마이그레이션 정상

---

**문의**: Backend Team



