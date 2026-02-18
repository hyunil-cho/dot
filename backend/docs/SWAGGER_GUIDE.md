# Swagger API 문서 가이드

## 📚 개요

Dot Backend API의 Swagger 문서가 구성되어 프론트엔드 개발자가 쉽게 API를 이해하고 사용할 수 있습니다.

---

## 🔗 Swagger UI 접속

### 로컬 개발 환경
애플리케이션 실행 후 아래 URL로 접속하세요:

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON 스펙
```
http://localhost:8080/v3/api-docs
```

---

## 🎯 Auth API 엔드포인트 목록

### 1. 회원가입 (POST /api/auth/signup)

**설명:** 이메일과 비밀번호로 새로운 계정을 생성합니다.

**요청 본문:**
```json
{
  "email": "user@example.com",
  "password": "Test1234!",
  "name": "홍길동"
}
```

**비밀번호 요구사항:**
- 최소 8자 이상
- 영문, 숫자, 특수문자(@$!%*#?&) 각 1개 이상 포함
- 예시: `Test1234!`

**응답:**
- `200 OK` - 회원가입 성공
- `400 Bad Request` - 이메일 형식 오류, 비밀번호 규칙 위반
- `409 Conflict` - 이미 존재하는 이메일

---

### 2. 로그인 (POST /api/auth/login)

**설명:** 이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.

**요청 본문:**
```json
{
  "email": "user@example.com",
  "password": "Test1234!"
}
```

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "user@example.com"
}
```

**발급되는 토큰:**
- **Access Token**: API 요청 시 사용 (유효기간: 15분)
- **Refresh Token**: Access Token 갱신 시 사용 (유효기간: 7일)

**토큰 사용 방법:**
1. 로그인 성공 시 `accessToken`과 `refreshToken`을 로컬에 저장
2. API 요청 시 헤더에 포함:
   ```
   Authorization: Bearer {accessToken}
   ```
3. Access Token 만료 시 `/api/auth/refresh`로 갱신

**응답:**
- `200 OK` - 로그인 성공, 토큰 발급
- `401 Unauthorized` - 이메일 또는 비밀번호 불일치

---

### 3. Access Token 갱신 (POST /api/auth/refresh)

**설명:** Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

**요청 본문:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**응답 예시:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

**사용 시나리오:**
- Access Token이 만료되었을 때 (401 Unauthorized 응답)
- 앱 재시작 시 저장된 Refresh Token으로 자동 로그인

**주의사항:**
- Refresh Token도 만료된 경우 재로그인 필요
- 새로운 Access Token만 발급되며, Refresh Token은 재발급되지 않음

**응답:**
- `200 OK` - 토큰 갱신 성공
- `401 Unauthorized` - 유효하지 않거나 만료된 Refresh Token

---

### 4. 로그아웃 (POST /api/auth/logout)

**설명:** Refresh Token을 무효화하여 로그아웃합니다.

**인증 필요:** ✅ (Bearer Token)

**요청 본문:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**처리 과정:**
1. 서버에서 해당 Refresh Token을 DB에서 삭제
2. 클라이언트는 저장된 Access Token과 Refresh Token을 제거

**응답:**
- `200 OK` - 로그아웃 성공
- `401 Unauthorized` - 인증 실패 (토큰 없음 또는 유효하지 않음)

---

### 5. 회원 탈퇴 (DELETE /api/auth/withdraw)

**설명:** 사용자 계정 및 관련된 모든 데이터를 영구 삭제합니다.

**인증 필요:** ✅ (Bearer Token)

**요청 본문:**
```json
{
  "password": "Test1234!",
  "reason": "서비스 이용이 불편해서"
}
```

**삭제되는 데이터:**
- 사용자 계정 정보
- 사용자가 생성한 모든 Persona
- 채팅 기록 및 세션
- 학습 데이터 및 AI 모델 (TODO: AI Engine 연동 필요)
- S3에 저장된 파일들

**주의사항:**
- 탈퇴 시 모든 데이터가 **영구 삭제**되며 복구 불가능
- 본인 확인을 위해 비밀번호 입력 필수
- 탈퇴 사유는 선택사항 (최대 500자)

**응답:**
- `204 No Content` - 회원 탈퇴 성공
- `400 Bad Request` - 비밀번호 불일치
- `401 Unauthorized` - 인증 실패

---

## 🔐 인증 방법

### Swagger UI에서 인증하기

1. Swagger UI 우측 상단의 **Authorize** 버튼 클릭
2. `bearerAuth` 섹션에 다음 형식으로 입력:
   ```
   Bearer {your_access_token}
   ```
3. **Authorize** 버튼 클릭
4. 이제 🔒 표시가 있는 엔드포인트 호출 가능

### 클라이언트에서 인증하기

모든 보호된 엔드포인트 요청 시 HTTP 헤더에 포함:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**예시 (Flutter/Dart):**
```dart
final response = await http.get(
  Uri.parse('http://localhost:8080/api/protected'),
  headers: {
    'Authorization': 'Bearer $accessToken',
    'Content-Type': 'application/json',
  },
);
```

---

## 📝 DTO 스키마 상세

### SignupRequest
| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| email | String | ✅ | 이메일 주소 (로그인 ID) | user@example.com |
| password | String | ✅ | 비밀번호 (영문+숫자+특수문자, 8자 이상) | Test1234! |
| name | String | ❌ | 사용자 이름 | 홍길동 |

### LoginRequest
| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| email | String | ✅ | 가입 시 사용한 이메일 | user@example.com |
| password | String | ✅ | 비밀번호 | Test1234! |

### LoginResponse
| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | JWT Access Token (15분) |
| refreshToken | String | JWT Refresh Token (7일) |
| tokenType | String | 토큰 타입 (항상 "Bearer") |
| userId | Long | 사용자 고유 ID |
| email | String | 사용자 이메일 |

### TokenResponse
| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | 새로 발급된 Access Token (15분) |
| tokenType | String | 토큰 타입 (항상 "Bearer") |

### WithdrawRequest
| 필드 | 타입 | 필수 | 설명 | 예시 |
|------|------|------|------|------|
| password | String | ✅ | 본인 확인용 현재 비밀번호 | Test1234! |
| reason | String | ❌ | 탈퇴 사유 (최대 500자) | 서비스 이용이 불편해서 |

### RefreshTokenRequest
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| refreshToken | String | ✅ | Refresh Token 값 |

---

## 🧪 테스트 시나리오

### 1. 회원가입 → 로그인 → 로그아웃

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

# 응답에서 accessToken과 refreshToken 저장

# 3. 로그아웃
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "refreshToken": "{refreshToken}"
  }'
```

### 2. 토큰 갱신 시나리오

```bash
# 1. 로그인하여 토큰 획득
# (위 시나리오 참조)

# 2. 15분 후 Access Token 만료 시 갱신
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "{refreshToken}"
  }'

# 새로운 accessToken 발급됨
```

### 3. 회원 탈퇴 시나리오

```bash
# 1. 로그인하여 토큰 획득

# 2. 회원 탈퇴
curl -X DELETE http://localhost:8080/api/auth/withdraw \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "password": "Test1234!",
    "reason": "테스트 완료"
  }'
```

---

## 🎨 프론트엔드 통합 가이드

### Flutter 예시

```dart
import 'package:http/http.dart' as http;
import 'dart:convert';

class AuthService {
  final String baseUrl = 'http://localhost:8080/api/auth';
  
  // 회원가입
  Future<void> signup(String email, String password, String? name) async {
    final response = await http.post(
      Uri.parse('$baseUrl/signup'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
        'name': name,
      }),
    );
    
    if (response.statusCode != 200) {
      throw Exception('회원가입 실패: ${response.body}');
    }
  }
  
  // 로그인
  Future<Map<String, dynamic>> login(String email, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'email': email,
        'password': password,
      }),
    );
    
    if (response.statusCode == 200) {
      return jsonDecode(response.body);
    } else {
      throw Exception('로그인 실패: ${response.body}');
    }
  }
  
  // 토큰 갱신
  Future<String> refreshToken(String refreshToken) async {
    final response = await http.post(
      Uri.parse('$baseUrl/refresh'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'refreshToken': refreshToken,
      }),
    );
    
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return data['accessToken'];
    } else {
      throw Exception('토큰 갱신 실패');
    }
  }
  
  // 로그아웃
  Future<void> logout(String accessToken, String refreshToken) async {
    final response = await http.post(
      Uri.parse('$baseUrl/logout'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $accessToken',
      },
      body: jsonEncode({
        'refreshToken': refreshToken,
      }),
    );
    
    if (response.statusCode != 200) {
      throw Exception('로그아웃 실패');
    }
  }
  
  // 회원 탈퇴
  Future<void> withdraw(String accessToken, String password, String? reason) async {
    final response = await http.delete(
      Uri.parse('$baseUrl/withdraw'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $accessToken',
      },
      body: jsonEncode({
        'password': password,
        'reason': reason,
      }),
    );
    
    if (response.statusCode != 204) {
      throw Exception('회원 탈퇴 실패: ${response.body}');
    }
  }
}
```

---

## 📞 문의 및 지원

- **Swagger UI 접속 문제**: 애플리케이션이 정상 실행 중인지 확인
- **인증 오류**: 토큰 형식이 `Bearer {token}` 형식인지 확인
- **CORS 오류**: 개발 환경에서는 CORS 설정 확인 필요

---

**문서 업데이트:** 2026-02-18

