package com.ccdd.project.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 阶段门准入核验规则实体 (GateEntryCriterion)
 * 映射 plm_project.gate_entry_criterion
 * 支持必选交付物齐套、M11验证证据覆盖度强核验 (AT-15 守护)、基线冻结状态检查
 */
public class GateEntryCriterionEntity {

    private Long criterionId;
    private Long gateId;
    private String criterionCode;
    private String name;
    private String ruleType; // DELIVERABLE_CHECK, EVIDENCE_COVERAGE, BASELINE_LOCKED
    private BigDecimal thresholdValue; // 如证据覆盖率 100.00
    private Boolean isBlocking; // 一票否决项
    private Instant createdAt;

    public GateEntryCriterionEntity() {
    }

    public GateEntryCriterionEntity(Long criterionId, Long gateId, String criterionCode, String name,
                                    String ruleType, BigDecimal thresholdValue, Boolean isBlocking, Instant createdAt) {
        this.criterionId = criterionId;
        this.gateId = gateId;
        this.criterionCode = criterionCode;
        this.name = name;
        this.ruleType = ruleType;
        this.thresholdValue = thresholdValue;
        this.isBlocking = isBlocking;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long criterionId;
        private Long gateId;
        private String criterionCode;
        private String name;
        private String ruleType;
        private BigDecimal thresholdValue;
        private Boolean isBlocking = true;
        private Instant createdAt = Instant.now();

        public Builder criterionId(Long criterionId) { this.criterionId = criterionId; return this; }
        public Builder gateId(Long gateId) { this.gateId = gateId; return this; }
        public Builder criterionCode(String criterionCode) { this.criterionCode = criterionCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder ruleType(String ruleType) { this.ruleType = ruleType; return this; }
        public Builder thresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; return this; }
        public Builder isBlocking(Boolean isBlocking) { this.isBlocking = isBlocking; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public GateEntryCriterionEntity build() {
            return new GateEntryCriterionEntity(criterionId, gateId, criterionCode, name, ruleType, thresholdValue, isBlocking, createdAt);
        }
    }

    public Long getCriterionId() { return criterionId; }
    public void setCriterionId(Long criterionId) { this.criterionId = criterionId; }
    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }
    public String getCriterionCode() { return criterionCode; }
    public void setCriterionCode(String criterionCode) { this.criterionCode = criterionCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public Boolean getIsBlocking() { return isBlocking; }
    public void setIsBlocking(Boolean blocking) { isBlocking = blocking; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
