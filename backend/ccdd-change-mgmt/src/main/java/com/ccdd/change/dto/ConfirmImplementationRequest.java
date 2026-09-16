package com.ccdd.change.dto;

/**
 * 确认现场实施回执请求 DTO (对账闭环)
 */
public class ConfirmImplementationRequest {

    private Long dispositionId;
    private String targetSystem; // MES, ERP, FIELD_CRM
    private Long externalReceiptId;
    private String executionStatus; // COMPLETED, FAILED
    private String siteOperatorId;
    private Long completionEvidenceDoc;

    public ConfirmImplementationRequest() {
        this.executionStatus = "COMPLETED";
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
}
