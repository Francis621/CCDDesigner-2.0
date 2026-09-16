package com.ccdd.change.entity;

import java.time.Instant;

/**
 * M22 专业影响处置决定实体 (ImpactDecision - 工程师签字裁定)
 * 固化专家决策与免责依据，防止系统误伤或漏判 (AT-10 规范)
 */
public class ImpactDecisionEntity {

    private Long decisionId;
    private Long impactItemId;
    private ImpactDecisionType decisionType;
    private String technicalRationale;
    private String actionRequired;
    private String targetActionPlan; // REVISE_EXISTING (升版), CREATE_NEW (新建件 - ADR-05)
    private String assessorId;
    private Instant assessedAt;

    public ImpactDecisionEntity() {
    }

    public ImpactDecisionEntity(Long decisionId, Long impactItemId, ImpactDecisionType decisionType,
                                String technicalRationale, String actionRequired, String targetActionPlan,
                                String assessorId, Instant assessedAt) {
        this.decisionId = decisionId;
        this.impactItemId = impactItemId;
        this.decisionType = decisionType;
        this.technicalRationale = technicalRationale;
        this.actionRequired = actionRequired;
        this.targetActionPlan = targetActionPlan;
        this.assessorId = assessorId;
        this.assessedAt = assessedAt;
    }

    public Long getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(Long decisionId) {
        this.decisionId = decisionId;
    }

    public Long getImpactItemId() {
        return impactItemId;
    }

    public void setImpactItemId(Long impactItemId) {
        this.impactItemId = impactItemId;
    }

    public ImpactDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(ImpactDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getTechnicalRationale() {
        return technicalRationale;
    }

    public void setTechnicalRationale(String technicalRationale) {
        this.technicalRationale = technicalRationale;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public String getTargetActionPlan() {
        return targetActionPlan;
    }

    public void setTargetActionPlan(String targetActionPlan) {
        this.targetActionPlan = targetActionPlan;
    }

    public String getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(String assessorId) {
        this.assessorId = assessorId;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(Instant assessedAt) {
        this.assessedAt = assessedAt;
    }
}
