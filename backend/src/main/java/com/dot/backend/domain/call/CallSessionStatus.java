package com.dot.backend.domain.call;

public enum CallSessionStatus {
    INIT,          // 초기화
    CONNECTING,    // 연결 중
    ACTIVE,        // 활성 (통화 중)
    ENDED          // 종료
}

