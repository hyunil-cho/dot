package com.dot.backend.exception;

/**
 * 잘못된 인증 정보 예외
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}

