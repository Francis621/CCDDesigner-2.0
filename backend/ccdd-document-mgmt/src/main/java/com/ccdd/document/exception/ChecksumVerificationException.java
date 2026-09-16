package com.ccdd.document.exception;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;

/**
 * 制品 SHA-256 完整性强校验失败异常 (触发 HTTP 422，阻断入库并销毁临时分片)
 */
public class ChecksumVerificationException extends BusinessException {

    private final String expectedHash;
    private final String actualHash;

    public ChecksumVerificationException(String message, String expectedHash, String actualHash) {
        super(ErrorCode.BAD_REQUEST, message);
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
    }

    public String getExpectedHash() {
        return expectedHash;
    }

    public String getActualHash() {
        return actualHash;
    }
}
