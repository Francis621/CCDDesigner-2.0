package com.ccdd.document.exception;

import com.ccdd.common.api.BusinessException;
import com.ccdd.common.api.ErrorCode;

/**
 * 协同排他悲观锁并发冲突异常 (触发 HTTP 409，阻断非持有者篡改)
 */
public class ConcurrencyLockViolationException extends BusinessException {

    private final String lockedByUserId;
    private final String attemptedUserId;

    public ConcurrencyLockViolationException(String message, String lockedByUserId, String attemptedUserId) {
        super(ErrorCode.CONFLICT, message);
        this.lockedByUserId = lockedByUserId;
        this.attemptedUserId = attemptedUserId;
    }

    public String getLockedByUserId() {
        return lockedByUserId;
    }

    public String getAttemptedUserId() {
        return attemptedUserId;
    }
}
