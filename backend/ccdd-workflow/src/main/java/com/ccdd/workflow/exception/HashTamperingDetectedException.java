package com.ccdd.workflow.exception;

/**
 * M24 AT-16 快照防篡改异常：审批中实体内容哈希发生漂移，触发安全熔断
 */
public class HashTamperingDetectedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HashTamperingDetectedException(String message) {
        super(message);
    }
}
