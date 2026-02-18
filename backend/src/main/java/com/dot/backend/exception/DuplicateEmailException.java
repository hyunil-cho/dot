package com.dot.backend.exception;

/**
 * 중복 이메일 예외
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("이미 사용 중인 이메일입니다: " + email);
    }
}

