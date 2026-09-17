package com.ccdd.workflow.dto;

import com.ccdd.workflow.entity.ApprovalConclusion;

import java.io.Serializable;
import java.time.Instant;

/**
 * 审批凭据核销结果响应 DTO (对齐 OpenAPI 8.3)
 */
public class ConsumeDecisionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long decisionTicketId;
    private ApprovalConclusion finalConclusion;
    private Boolean isConsumed;
    private Boolean verified;
    private Instant consumedAt;
    private String cryptoSignatureStamp;

    public ConsumeDecisionResponse() {
    }

    public ConsumeDecisionResponse(Long decisionTicketId, ApprovalConclusion finalConclusion, Boolean isConsumed,
                                   Boolean verified, Instant consumedAt, String cryptoSignatureStamp) {
        this.decisionTicketId = decisionTicketId;
        this.finalConclusion = finalConclusion;
        this.isConsumed = isConsumed;
        this.verified = verified;
        this.consumedAt = consumedAt;
        this.cryptoSignatureStamp = cryptoSignatureStamp;
    }

    public Long getDecisionTicketId() {
        return decisionTicketId;
    }

    public void setDecisionTicketId(Long decisionTicketId) {
        this.decisionTicketId = decisionTicketId;
    }

    public ApprovalConclusion getFinalConclusion() {
        return finalConclusion;
    }

    public void setFinalConclusion(ApprovalConclusion finalConclusion) {
        this.finalConclusion = finalConclusion;
    }

    public Boolean getIsConsumed() {
        return isConsumed;
    }

    public void setIsConsumed(Boolean consumed) {
        isConsumed = consumed;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getCryptoSignatureStamp() {
        return cryptoSignatureStamp;
    }

    public void setCryptoSignatureStamp(String cryptoSignatureStamp) {
        this.cryptoSignatureStamp = cryptoSignatureStamp;
    }
}
