package com.ccdd.change.exception;

/**
 * 影响分析未完全裁决异常 (CST-M22-02 约束)
 * 当变更影响候选集中存在未完成专业裁决或拓扑截断时，尝试提交 CCB 授权审批时抛出
 */
public class IncompleteImpactAssessmentException extends RuntimeException {

    private final Long ecoId;
    private final int unassessedCount;

    public IncompleteImpactAssessmentException(String message) {
        super(message);
        this.ecoId = null;
        this.unassessedCount = 0;
    }

    public IncompleteImpactAssessmentException(Long ecoId, int unassessedCount, String message) {
        super(message);
        this.ecoId = ecoId;
        this.unassessedCount = unassessedCount;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public int getUnassessedCount() {
        return unassessedCount;
    }
}
