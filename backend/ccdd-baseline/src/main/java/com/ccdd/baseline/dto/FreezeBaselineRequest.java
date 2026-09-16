package com.ccdd.baseline.dto;

/**
 * 审批冻结基线请求 DTO
 */
public class FreezeBaselineRequest {

    private Long baselineId;
    private String approver;
    private String approvalComments;
    private Long approvalTicketId;
    private Boolean enforceClosureValidation;

    public FreezeBaselineRequest() {
        this.enforceClosureValidation = true;
    }

    public FreezeBaselineRequest(Long baselineId, String approver, String approvalComments, Long approvalTicketId) {
        this.baselineId = baselineId;
        this.approver = approver;
        this.approvalComments = approvalComments;
        this.approvalTicketId = approvalTicketId;
        this.enforceClosureValidation = true;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApprovalComments() {
        return approvalComments;
    }

    public void setApprovalComments(String approvalComments) {
        this.approvalComments = approvalComments;
    }

    public Long getApprovalTicketId() {
        return approvalTicketId;
    }

    public void setApprovalTicketId(Long approvalTicketId) {
        this.approvalTicketId = approvalTicketId;
    }

    public Boolean getEnforceClosureValidation() {
        return enforceClosureValidation;
    }

    public void setEnforceClosureValidation(Boolean enforceClosureValidation) {
        this.enforceClosureValidation = enforceClosureValidation;
    }
}
