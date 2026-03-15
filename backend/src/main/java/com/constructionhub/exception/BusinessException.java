package com.constructionhub.exception;

/**
 * Thrown for expected business-rule violations (duplicate email, invalid state transitions, etc.).
 * Mapped to HTTP 400 by GlobalExceptionHandler — keeps RuntimeException free for truly unexpected errors.
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
