package com.ccdd.message.exception;

/**
 * 邮件基础权限拒绝异常 (试图读取非自身个人投递箱信件)
 */
public class MessageAccessDeniedException extends RuntimeException {

    private final String errorCode;

    public MessageAccessDeniedException(String message) {
        super(message);
        this.errorCode = "ERR_MESSAGE_ACCESS_DENIED";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
