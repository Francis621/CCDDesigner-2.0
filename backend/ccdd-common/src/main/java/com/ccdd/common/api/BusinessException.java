package com.ccdd.common.api;

import lombok.Getter;

/**
 * 平台统一业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final Object errorDetails;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorDetails = null;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorDetails = null;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.getCode();
        this.errorDetails = null;
    }

    public BusinessException(ErrorCode errorCode, Object errorDetails) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorDetails = errorDetails;
    }
}
