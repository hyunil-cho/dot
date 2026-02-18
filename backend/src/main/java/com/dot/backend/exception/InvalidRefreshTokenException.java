package com.dot.backend.exception;

/**
 * 유효하지 않은 Refresh Token 예외
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("유효하지 않거나 만료된 리프레시 토큰입니다");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}

