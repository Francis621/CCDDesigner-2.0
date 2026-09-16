package com.ccdd.change.exception;

/**
 * 违反互换性准则异常 (ADR-05 落地)
 * 当变更引起形状、配合或功能 (FFF) 改变无法双向互换，却尝试盲目对既有物料升版时抛出
 */
public class IncompatibleInterchangeabilityException extends RuntimeException {

    private final String partNumber;
    private final String fffImpactReason;

    public IncompatibleInterchangeabilityException(String message) {
        super(message);
        this.partNumber = null;
        this.fffImpactReason = null;
    }

    public IncompatibleInterchangeabilityException(String partNumber, String fffImpactReason, String message) {
        super(message);
        this.partNumber = partNumber;
        this.fffImpactReason = fffImpactReason;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public String getFffImpactReason() {
        return fffImpactReason;
    }
}
