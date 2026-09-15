package com.ccdd.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 RESTful API 响应包络契约
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 业务响应码 (200 为成功) */
    private int code;

    /** 响应描述或错误原因 */
    private String message;

    /** 业务数据负载 */
    private T data;

    /** 链路追踪与关联标识 */
    private String correlationId;

    /** 成功便捷构造器 */
    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .code(200)
                .message("Operation Successful")
                .data(data)
                .build();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    /** 失败构造器 */
    public static <T> Result<T> error(int code, String message) {
        return Result.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }
}
