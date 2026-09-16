package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 基线演进系谱关联实体 (SuccessorBaselineLink)
 * 记录基线间受控替代与版本演化关系
 */
public class SuccessorBaselineLinkEntity {

    private Long linkId;
    private Long predecessorBaselineId;
    private Long successorBaselineId;
    private Long changeOrderId;
    private String derivationReason;
    private Instant linkedAt;

    public SuccessorBaselineLinkEntity() {
    }

    public SuccessorBaselineLinkEntity(Long linkId, Long predecessorBaselineId, Long successorBaselineId,
                                       Long changeOrderId, String derivationReason, Instant linkedAt) {
        this.linkId = linkId;
        this.predecessorBaselineId = predecessorBaselineId;
        this.successorBaselineId = successorBaselineId;
        this.changeOrderId = changeOrderId;
        this.derivationReason = derivationReason;
        this.linkedAt = linkedAt;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    public Long getPredecessorBaselineId() {
        return predecessorBaselineId;
    }

    public void setPredecessorBaselineId(Long predecessorBaselineId) {
        this.predecessorBaselineId = predecessorBaselineId;
    }

    public Long getSuccessorBaselineId() {
        return successorBaselineId;
    }

    public void setSuccessorBaselineId(Long successorBaselineId) {
        this.successorBaselineId = successorBaselineId;
    }

    public Long getChangeOrderId() {
        return changeOrderId;
    }

    public void setChangeOrderId(Long changeOrderId) {
        this.changeOrderId = changeOrderId;
    }

    public String getDerivationReason() {
        return derivationReason;
    }

    public void setDerivationReason(String derivationReason) {
        this.derivationReason = derivationReason;
    }

    public Instant getLinkedAt() {
        return linkedAt;
    }

    public void setLinkedAt(Instant linkedAt) {
        this.linkedAt = linkedAt;
    }
}
