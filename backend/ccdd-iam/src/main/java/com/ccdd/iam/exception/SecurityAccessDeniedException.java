package com.ccdd.iam.exception;

/**
 * 通用安全访问被拒异常
 */
public class SecurityAccessDeniedException extends RuntimeException {

    private final String errorCode;

    public SecurityAccessDeniedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
