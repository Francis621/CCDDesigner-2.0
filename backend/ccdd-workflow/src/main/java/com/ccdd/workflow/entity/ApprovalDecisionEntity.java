package com.ccdd.workflow.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M24: 不可伪造审批决议凭证实体 (核心法律凭据，状态机凭此推进，无权改写业务数据)
 */
public class ApprovalDecisionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long decisionTicketId;
    private Long workflowInstId;
    private String targetObjectType;
    private Long targetObjectId;
    private String targetContentHash;
    private ApprovalConclusion finalConclusion;
    private Boolean isConsumed;
    private Instant consumedAt;
    private String consumedByAction;
    private String cryptoSignatureStamp;
    private String signedPayloadDigest;
    private Instant decidedAt;

    public ApprovalDecisionEntity() {
    }

    public ApprovalDecisionEntity(Long decisionTicketId, Long workflowInstId, String targetObjectType,
                                  Long targetObjectId, String targetContentHash, ApprovalConclusion finalConclusion,
                                  Boolean isConsumed, Instant consumedAt, String consumedByAction,
                                  String cryptoSignatureStamp, String signedPayloadDigest, Instant decidedAt) {
        this.decisionTicketId = decisionTicketId;
        this.workflowInstId = workflowInstId;
        this.targetObjectType = targetObjectType;
        this.targetObjectId = targetObjectId;
        this.targetContentHash = targetContentHash;
        this.finalConclusion = finalConclusion;
        this.isConsumed = isConsumed;
        this.consumedAt = consumedAt;
        this.consumedByAction = consumedByAction;
        this.cryptoSignatureStamp = cryptoSignatureStamp;
        this.signedPayloadDigest = signedPayloadDigest;
        this.decidedAt = decidedAt;
    }

    public Long getDecisionTicketId() {
        return decisionTicketId;
    }

    public void setDecisionTicketId(Long decisionTicketId) {
        this.decisionTicketId = decisionTicketId;
    }

    public Long getWorkflowInstId() {
        return workflowInstId;
    }

    public void setWorkflowInstId(Long workflowInstId) {
        this.workflowInstId = workflowInstId;
    }

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public Long getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(Long targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public String getTargetContentHash() {
        return targetContentHash;
    }

    public void setTargetContentHash(String targetContentHash) {
        this.targetContentHash = targetContentHash;
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

    public Instant getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getConsumedByAction() {
        return consumedByAction;
    }

    public void setConsumedByAction(String consumedByAction) {
        this.consumedByAction = consumedByAction;
    }

    public String getCryptoSignatureStamp() {
        return cryptoSignatureStamp;
    }

    public void setCryptoSignatureStamp(String cryptoSignatureStamp) {
        this.cryptoSignatureStamp = cryptoSignatureStamp;
    }

    public String getSignedPayloadDigest() {
        return signedPayloadDigest;
    }

    public void setSignedPayloadDigest(String signedPayloadDigest) {
        this.signedPayloadDigest = signedPayloadDigest;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}
