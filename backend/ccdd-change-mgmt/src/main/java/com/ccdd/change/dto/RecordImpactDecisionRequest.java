package com.ccdd.change.dto;

import com.ccdd.change.entity.ImpactDecisionType;

/**
 * 录入专业影响处置决定请求 DTO (OpenAPI §8.2)
 */
public class RecordImpactDecisionRequest {

    private Long impactItemId;
    private ImpactDecisionType decisionType; // MODIFY, RE_VERIFY, REVIEW_ONLY, NO_IMPACT
    private String technicalRationale;      // 专业分析依据 (判 NO_IMPACT 必须填写免责依据)
    private String targetActionPlan;        // REVISE_EXISTING (升版), CREATE_NEW (新建件 - ADR-05)
    private String actionRequired;          // 指派给后继任务的具体工程要求
    private String assessorId;

    public RecordImpactDecisionRequest() {
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

    public String getTargetActionPlan() {
        return targetActionPlan;
    }

    public void setTargetActionPlan(String targetActionPlan) {
        this.targetActionPlan = targetActionPlan;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public String getAssessorId() {
        return assessorId;
    }

    public void setAssessorId(String assessorId) {
        this.assessorId = assessorId;
    }
}
