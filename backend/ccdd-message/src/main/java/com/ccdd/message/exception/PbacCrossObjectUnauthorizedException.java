package com.ccdd.message.exception;

/**
 * 消息直达业务对象跨域无权防穿透拦截异常 (403 Forbidden)
 * 对齐 TC-MSG-02 防穿透与 PBAC 强校验规格
 */
public class PbacCrossObjectUnauthorizedException extends RuntimeException {

    private final String errorCode;

    public PbacCrossObjectUnauthorizedException(String message) {
        super(message);
        this.errorCode = "ERR_PBAC_CROSS_OBJECT_UNAUTHORIZED";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
