package com.ccdd.common.api;

import java.io.Serializable;

/**
 * 统一 RESTful API 响应包络契约 (纯原生 Java 实现，兼容各 JDK 版本)
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private String correlationId;

    public Result() {
    }

    public Result(int code, String message, T data, String correlationId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.correlationId = correlationId;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Operation Successful", data, null);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null, null);
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
