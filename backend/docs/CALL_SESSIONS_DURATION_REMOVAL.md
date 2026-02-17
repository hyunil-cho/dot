# CALL_SESSIONS duration_seconds 제거 작업 완료

## ✅ 작업 완료 내역

### 🎯 목적
`call_sessions` 테이블의 `duration_seconds` 컬럼을 제거하고 동적 계산으로 전환하여 데이터 정합성 향상

---

## 📊 변경 전/후 비교

### 변경 전
```sql
call_sessions
├── started_at          -- 시작 시간
├── ended_at            -- 종료 시간
└── duration_seconds    -- 통화 시간 (중복!) ❌
```

**문제점**:
- duration_seconds는 started_at과 ended_at으로 계산 가능
- 데이터 중복으로 정합성 문제 발생 가능
- duration을 업데이트하는 로직 필요

---

### 변경 후
```sql
call_sessions
├── started_at          -- 시작 시간 ✅
└── ended_at            -- 종료 시간 ✅
    (duration은 동적 계산)
```

**개선사항**:
- ✅ 단일 진실 공급원 (Single Source of Truth)
- ✅ 데이터 정합성 보장
- ✅ 코드 간결성 향상

---

## 🔧 수정된 파일

### 1. Entity 클래스 수정

#### `CallSession.java`

**제거된 필드**:
```java
@Column(name = "duration_seconds")
private Integer durationSeconds; // ❌ 제거
```

**추가된 메서드**:
```java
/**
 * 통화 시간을 동적으로 계산
 * @return 통화 시간 (초), 시작/종료 시간이 없으면 null
 */
public Integer getDurationSeconds() {
    if (this.startedAt == null || this.endedAt == null) {
        return null;
    }
    return (int) Duration.between(this.startedAt, this.endedAt).getSeconds();
}
```

**수정된 메서드**:
```java
// Before: duration 수동 계산 및 저장
public void end() {
    this.status = CallSessionStatus.ENDED;
    this.endedAt = LocalDateTime.now();
    
    if (this.startedAt != null) {
        this.durationSeconds = (int) Duration.between(this.startedAt, this.endedAt).getSeconds();
    }
}

// After: ended_at만 설정
public void end() {
    this.status = CallSessionStatus.ENDED;
    this.endedAt = LocalDateTime.now();
    // getDurationSeconds()는 자동 계산됨
}
```

---

### 2. 마이그레이션 스크립트 수정

#### `V5__create_call_sessions_table.sql`

**변경 전**:
```sql
CREATE TABLE call_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INIT',
    started_at DATETIME(6) DEFAULT NULL,
    ended_at DATETIME(6) DEFAULT NULL,
    duration_seconds INT DEFAULT NULL,  -- ❌ 제거됨
    ...
);
```

**변경 후**:
```sql
CREATE TABLE call_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    persona_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INIT',
    started_at DATETIME(6) DEFAULT NULL,
    ended_at DATETIME(6) DEFAULT NULL,
    -- duration_seconds 제거 ✅
    ...
);
```

---

### 3. 문서 업데이트

#### `docs/DATABASE_SCHEMA.md`

**추가된 섹션**:
```markdown
### CALL_SESSIONS의 duration_seconds 제거 ✅

**결정**: 통화 시간은 `started_at`과 `ended_at`으로 동적 계산

**이유**:
- ✅ 단일 진실 공급원
- ✅ 데이터 정합성 보장
- ✅ 계산 비용 무시 가능
```

**ERD 수정**:
- `call_sessions`의 `duration_seconds` 필드 제거

**테이블 설명 업데이트**:
- 동적 계산 방식 명시
- 코드 예시 추가

**쿼리 예시 추가**:
```java
CallSession session = callSessionRepository.findById(sessionId).orElseThrow();
Integer duration = session.getDurationSeconds(); // 동적 계산
```

---

## 📋 CallLog는 duration_seconds 유지

### 이유

**call_sessions** (진행 중인 세션):
- 동적 계산으로 충분
- 실시간 상태 관리가 목적

**call_logs** (이력 조회용):
- 성능 최적화를 위해 미리 계산된 값 저장
- 조회 빈도가 높으므로 매번 계산하는 것보다 저장이 유리

### CallLog 생성 시

```java
// CallLog.fromSession() 메서드에서 한 번만 계산
public static CallLog fromSession(CallSession session) {
    return CallLog.builder()
            .user(session.getUser())
            .persona(session.getPersona())
            .callSession(session)
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .durationSeconds(session.getDurationSeconds()) // 동적 계산된 값 저장
            .build();
}
```

---

## 🎯 아키텍처 원칙

### 단일 진실 공급원 (Single Source of Truth)

**원칙**:
- 데이터는 한 곳에만 저장
- 파생 값은 계산으로 도출

**적용**:
```java
// Source of Truth
private LocalDateTime startedAt;
private LocalDateTime endedAt;

// Derived Value (계산)
public Integer getDurationSeconds() {
    return (int) Duration.between(startedAt, endedAt).getSeconds();
}
```

**장점**:
- 데이터 불일치 방지
- 업데이트 로직 단순화
- 버그 발생 가능성 감소

---

## 🚀 사용 예시

### Service Layer에서 사용

```java
@Service
public class CallSessionService {
    
    @Transactional
    public void endCall(Long sessionId) {
        CallSession session = callSessionRepository.findById(sessionId)
            .orElseThrow();
        
        // 세션 종료 (ended_at만 설정)
        session.end();
        callSessionRepository.save(session);
        
        // CallLog 생성 (duration은 자동 계산됨)
        CallLog log = CallLog.fromSession(session);
        callLogRepository.save(log);
    }
    
    public CallSessionDto getSessionDetails(Long sessionId) {
        CallSession session = callSessionRepository.findById(sessionId)
            .orElseThrow();
        
        return CallSessionDto.builder()
            .id(session.getId())
            .status(session.getStatus())
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .durationSeconds(session.getDurationSeconds()) // Getter 호출
            .build();
    }
}
```

### Controller에서 응답

```java
@GetMapping("/{id}")
public ResponseEntity<CallSessionResponse> getSession(@PathVariable Long id) {
    CallSession session = callSessionRepository.findById(id).orElseThrow();
    
    return ResponseEntity.ok(
        CallSessionResponse.builder()
            .id(session.getId())
            .status(session.getStatus().name())
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .durationSeconds(session.getDurationSeconds()) // 동적 계산
            .build()
    );
}
```

---

## ✅ 테스트 시나리오

### 1. 진행 중인 세션
```java
CallSession session = new CallSession();
session.setStartedAt(LocalDateTime.now());
// ended_at이 없으므로
assertNull(session.getDurationSeconds());
```

### 2. 종료된 세션
```java
CallSession session = new CallSession();
session.setStartedAt(LocalDateTime.now().minusMinutes(5));
session.end(); // ended_at 설정

Integer duration = session.getDurationSeconds();
assertNotNull(duration);
assertTrue(duration >= 300); // 약 5분 (300초)
```

### 3. CallLog 생성
```java
CallSession session = createEndedSession();
CallLog log = CallLog.fromSession(session);

assertEquals(session.getDurationSeconds(), log.getDurationSeconds());
```

---

## 🔍 성능 영향 분석

### 계산 비용

```java
// Duration.between() 성능
Duration.between(start, end).getSeconds();
// O(1) 복잡도, 나노초 단위 연산
// 실제 소요 시간: < 1 마이크로초
```

**결론**: 성능 영향 무시 가능

### 조회 성능

**call_sessions**:
- 진행 중인 세션 조회는 빈도가 낮음
- 동적 계산으로도 충분

**call_logs**:
- 이력 조회는 빈도가 높음
- 미리 계산된 값 저장 (성능 최적화)

---

## 📊 변경 요약

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| **call_sessions 테이블** | `duration_seconds` 컬럼 존재 | 컬럼 제거 ✅ |
| **CallSession Entity** | `durationSeconds` 필드 | Getter 메서드로 계산 ✅ |
| **end() 메서드** | duration 수동 계산 및 저장 | ended_at만 설정 ✅ |
| **call_logs 테이블** | `duration_seconds` 유지 | 유지 (성능) ✅ |
| **데이터 정합성** | 중복 데이터로 불일치 가능 | 단일 진실 공급원 ✅ |

---

## 🎉 작업 완료!

**빌드 상태**:
```
BUILD SUCCESSFUL in 6s ✅
```

**변경된 파일**:
- ✅ `CallSession.java` - 필드 제거, Getter 추가
- ✅ `V5__create_call_sessions_table.sql` - 컬럼 제거
- ✅ `docs/DATABASE_SCHEMA.md` - 문서 업데이트

**영향 받지 않는 파일**:
- ✅ `CallLog.java` - duration_seconds 유지
- ✅ `V6__create_call_logs_table.sql` - 변경 없음

---

## 📝 다음 단계

이제 Service Layer 구현 시:

```java
// 통화 종료
session.end();
// duration은 자동 계산됨

// CallLog 생성
CallLog log = CallLog.fromSession(session);
// session.getDurationSeconds()가 log.durationSeconds에 복사됨
```

간단하고 명확한 코드 작성 가능! 🚀

