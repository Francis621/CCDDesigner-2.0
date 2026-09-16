package com.ccdd.change.entity;

import java.time.Instant;

/**
 * M22 变更现场实施回执跟踪实体 (ImplementationRecord - 跨系统对账)
 * 严格依赖 MES/ERP/现场服务项级回执核验真实闭环 (AT-22 约束)
 */
public class ImplementationRecordEntity {

    private Long recordId;
    private Long ecoId;
    private Long dispositionId;
    private String targetSystem; // MES, ERP, FIELD_CRM
    private Long externalReceiptId;
    private String executionStatus; // DISPATCHED, IN_EXECUTION, COMPLETED, FAILED
    private String siteOperatorId;
    private Long completionEvidenceDoc;
    private Instant confirmedAt;
    private Instant createdAt;

    public ImplementationRecordEntity() {
    }

    public ImplementationRecordEntity(Long recordId, Long ecoId, Long dispositionId,
                                      String targetSystem, Long externalReceiptId,
                                      String executionStatus, String siteOperatorId,
                                      Long completionEvidenceDoc, Instant confirmedAt, Instant createdAt) {
        this.recordId = recordId;
        this.ecoId = ecoId;
        this.dispositionId = dispositionId;
        this.targetSystem = targetSystem;
        this.externalReceiptId = externalReceiptId;
        this.executionStatus = executionStatus;
        this.siteOperatorId = siteOperatorId;
        this.completionEvidenceDoc = completionEvidenceDoc;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public Long getDispositionId() {
        return dispositionId;
    }

    public void setDispositionId(Long dispositionId) {
        this.dispositionId = dispositionId;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    public Long getExternalReceiptId() {
        return externalReceiptId;
    }

    public void setExternalReceiptId(Long externalReceiptId) {
        this.externalReceiptId = externalReceiptId;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getSiteOperatorId() {
        return siteOperatorId;
    }

    public void setSiteOperatorId(String siteOperatorId) {
        this.siteOperatorId = siteOperatorId;
    }

    public Long getCompletionEvidenceDoc() {
        return completionEvidenceDoc;
    }

    public void setCompletionEvidenceDoc(Long completionEvidenceDoc) {
        this.completionEvidenceDoc = completionEvidenceDoc;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
