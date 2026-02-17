package com.dot.backend.exception;

/**
 * 중복 이메일 예외
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}

