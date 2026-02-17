# TODO List - Dot Backend

## 🚧 회원 탈퇴 기능 관련 TODO

### AI 엔진 연동 (우선순위: 높음)

#### 1. AI Engine 모델 삭제 API 확인
- [ ] **AI Engine팀과 협의**
  - AI Engine에 Persona 학습 모델 삭제 API가 존재하는지 확인
  - API 스펙 확인 필요 사항:
    - 엔드포인트: `DELETE /api/v1/models/{userId}` 또는 유사
    - 요청 파라미터: userId, personaIds 등
    - 응답 형식: 성공/실패 상태 코드
    - 비동기 처리 여부
    - 에러 케이스: 학습 중인 모델, 존재하지 않는 모델 등

#### 2. AuthService.withdraw()에 AI 모델 삭제 로직 추가
**파일**: `src/main/java/com/dot/backend/service/AuthService.java`

**현재 코드 위치**:
```java
// 2. TODO: AI 모델 삭제 요청
// ⚠️ AI Engine API 확인 후 구현 필요
```

**구현 예시**:
```java
// AI Engine API 확인 후 주석 제거 및 구현
try {
    aiEngineClient.deletePersonaModels(currentUser.getId());
    log.info("AI models deleted for user: {}", currentUser.getId());
} catch (AiEngineException e) {
    log.error("Failed to delete AI models for user: {}", currentUser.getId(), e);
    // 실패 시 전략 선택 (아래 3번 참조)
}
```

#### 3. AI 모델 삭제 실패 시 전략 수립
- [ ] **전략 결정 필요**
  
  **옵션 A: 트랜잭션 롤백 (일관성 우선)**
  ```java
  @Transactional
  public void withdraw(...) {
      aiEngineClient.deletePersonaModels(...);  // 실패 시 예외 발생
      userRepository.delete(currentUser);        // 롤백됨
  }
  ```
  - 장점: 데이터 일관성 보장
  - 단점: AI Engine 장애 시 탈퇴 불가

  **옵션 B: 보상 트랜잭션 (가용성 우선)**
  ```java
  @Transactional
  public void withdraw(...) {
      userRepository.delete(currentUser);  // 먼저 삭제
      
      // 비동기로 AI 모델 삭제 시도
      asyncService.deleteAiModelsAsync(currentUser.getId());
  }
  ```
  - 장점: 탈퇴 항상 가능
  - 단점: AI 모델이 남을 수 있음

  **옵션 C: 수동 정리 큐 (관리자 개입)**
  ```java
  @Transactional
  public void withdraw(...) {
      try {
          aiEngineClient.deletePersonaModels(...);
      } catch (Exception e) {
          // 실패한 작업을 큐에 저장
          cleanupQueue.add(new CleanupTask(currentUser.getId()));
      }
      userRepository.delete(currentUser);
  }
  ```
  - 장점: 유연한 처리
  - 단점: 추가 구현 필요

#### 4. AiApiClient 인터페이스 확장
**파일**: `src/main/java/com/dot/backend/client/ai/AiApiClient.java`

**추가 필요 메서드**:
```java
public interface AiApiClient {
    // ...existing code...

    /**
     * 사용자의 모든 Persona 학습 모델 삭제
     * 
     * @param userId 사용자 ID
     * @throws AiEngineException AI Engine 통신 실패 시
     */
    void deletePersonaModels(Long userId);

    /**
     * 특정 Persona의 학습 모델 삭제
     * 
     * @param personaId Persona ID
     * @throws AiEngineException AI Engine 통신 실패 시
     */
    void deletePersonaModel(Long personaId);
}
```

---

## 📋 기타 TODO

### 회원 탈퇴 기능 개선
- [ ] 탈퇴 사유 통계 대시보드 구현
- [ ] 탈퇴 전 확인 메시지 UI (프론트엔드)
- [ ] 탈퇴 후 데이터 백업 정책 수립 (법적 요구사항 확인)

### 테스트 코드 작성
- [ ] `AuthServiceTest.withdraw()` 단위 테스트
- [ ] `AuthControllerTest.withdraw()` 통합 테스트
- [ ] CASCADE 삭제 검증 테스트
- [ ] 비밀번호 불일치 시 예외 처리 테스트

### API 문서 업데이트
- [ ] Swagger/OpenAPI 명세에 `DELETE /api/auth/withdraw` 추가
- [ ] Postman Collection 업데이트
- [ ] `docs/JWT_AUTH_IMPLEMENTATION.md`에 탈퇴 API 추가

---

## 🔗 관련 문서
- `PROJECT_PLAN.md` - FR-01-03 회원탈퇴 요구사항
- `docs/DATABASE_SCHEMA.md` - CASCADE 삭제 정책
- `docs/JWT_AUTH_IMPLEMENTATION.md` - JWT 인증 구조

---

**최종 업데이트**: 2026-02-18

