package com.ccdd.message.exception;

/**
 * 客户端伪造系统消息防篡改异常 (422 Unprocessable Entity)
 * 对齐 TC-MSG-06 防伪造与防篡改规格
 */
public class ManualSystemMessageForbiddenException extends RuntimeException {

    private final String errorCode;

    public ManualSystemMessageForbiddenException(String message) {
        super(message);
        this.errorCode = "ERR_MANUAL_SYSTEM_MESSAGE_FORBIDDEN";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
