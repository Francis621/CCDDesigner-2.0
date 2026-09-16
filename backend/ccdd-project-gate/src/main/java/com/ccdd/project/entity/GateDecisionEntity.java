package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 阶段门评审决策实体 (GateDecision - 不可变凭证)
 * 映射 plm_project.gate_decision
 * 包含四类法定决策类型，CONDITIONAL_PASS 时必须具备明确允许放行范围 (allowedScope)
 */
public class GateDecisionEntity {

    private Long decisionId;
    private Long gateId;
    private GateDecisionType decisionType;
    private String decisionNotes;
    private Long evaluatedBaselineId;
    private Long approvalTicketId;
    private String allowedScope; // CONDITIONAL_PASS 时必须有值
    private String decisionMakerId;
    private Instant decidedAt;

    public GateDecisionEntity() {
    }

    public GateDecisionEntity(Long decisionId, Long gateId, GateDecisionType decisionType, String decisionNotes,
                              Long evaluatedBaselineId, Long approvalTicketId, String allowedScope,
                              String decisionMakerId, Instant decidedAt) {
        this.decisionId = decisionId;
        this.gateId = gateId;
        this.decisionType = decisionType;
        this.decisionNotes = decisionNotes;
        this.evaluatedBaselineId = evaluatedBaselineId;
        this.approvalTicketId = approvalTicketId;
        this.allowedScope = allowedScope;
        this.decisionMakerId = decisionMakerId;
        this.decidedAt = decidedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long decisionId;
        private Long gateId;
        private GateDecisionType decisionType;
        private String decisionNotes;
        private Long evaluatedBaselineId;
        private Long approvalTicketId;
        private String allowedScope;
        private String decisionMakerId;
        private Instant decidedAt = Instant.now();

        public Builder decisionId(Long decisionId) { this.decisionId = decisionId; return this; }
        public Builder gateId(Long gateId) { this.gateId = gateId; return this; }
        public Builder decisionType(GateDecisionType decisionType) { this.decisionType = decisionType; return this; }
        public Builder decisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; return this; }
        public Builder evaluatedBaselineId(Long evaluatedBaselineId) { this.evaluatedBaselineId = evaluatedBaselineId; return this; }
        public Builder approvalTicketId(Long approvalTicketId) { this.approvalTicketId = approvalTicketId; return this; }
        public Builder allowedScope(String allowedScope) { this.allowedScope = allowedScope; return this; }
        public Builder decisionMakerId(String decisionMakerId) { this.decisionMakerId = decisionMakerId; return this; }
        public Builder decidedAt(Instant decidedAt) { this.decidedAt = decidedAt; return this; }

        public GateDecisionEntity build() {
            return new GateDecisionEntity(decisionId, gateId, decisionType, decisionNotes,
                    evaluatedBaselineId, approvalTicketId, allowedScope, decisionMakerId, decidedAt);
        }
    }

    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }
    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }
    public GateDecisionType getDecisionType() { return decisionType; }
    public void setDecisionType(GateDecisionType decisionType) { this.decisionType = decisionType; }
    public String getDecisionNotes() { return decisionNotes; }
    public void setDecisionNotes(String decisionNotes) { this.decisionNotes = decisionNotes; }
    public Long getEvaluatedBaselineId() { return evaluatedBaselineId; }
    public void setEvaluatedBaselineId(Long evaluatedBaselineId) { this.evaluatedBaselineId = evaluatedBaselineId; }
    public Long getApprovalTicketId() { return approvalTicketId; }
    public void setApprovalTicketId(Long approvalTicketId) { this.approvalTicketId = approvalTicketId; }
    public String getAllowedScope() { return allowedScope; }
    public void setAllowedScope(String allowedScope) { this.allowedScope = allowedScope; }
    public String getDecisionMakerId() { return decisionMakerId; }
    public void setDecisionMakerId(String decisionMakerId) { this.decisionMakerId = decisionMakerId; }
    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
