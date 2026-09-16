package com.ccdd.change.exception;

/**
 * 现场实施未闭环异常 (AT-22 守卫，CST-M22-01 约束)
 * 当 ECO 处于 RELEASED 尝试流转至 CLOSED，但现场实施回执仍有未完成项时抛出
 */
public class FieldExecutionIncompleteException extends RuntimeException {

    private final Long ecoId;
    private final int pendingReceiptCount;

    public FieldExecutionIncompleteException(String message) {
        super(message);
        this.ecoId = null;
        this.pendingReceiptCount = 0;
    }

    public FieldExecutionIncompleteException(Long ecoId, int pendingReceiptCount, String message) {
        super(message);
        this.ecoId = ecoId;
        this.pendingReceiptCount = pendingReceiptCount;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public int getPendingReceiptCount() {
        return pendingReceiptCount;
    }
}
