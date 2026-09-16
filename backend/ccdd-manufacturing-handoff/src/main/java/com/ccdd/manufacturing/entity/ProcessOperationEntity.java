package com.ccdd.manufacturing.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 工艺工序实体 (BOP Process Operation)
 * 映射物理表 sys_process_operations
 */
public class ProcessOperationEntity {

    private Long operationId;
    private String tenantId;
    private Long planId;
    private Integer sequenceNumber;
    private String operationCode;
    private String operationName;
    private String workCenterCode;
    private BigDecimal setupTimeMins;
    private BigDecimal runTimeMins;
    private String toolingFixtures;
    private String inspectionRequirement;
    private Instant createdAt;

    public ProcessOperationEntity() {
    }

    public ProcessOperationEntity(Long operationId, String tenantId, Long planId, Integer sequenceNumber,
                                  String operationCode, String operationName, String workCenterCode,
                                  BigDecimal setupTimeMins, BigDecimal runTimeMins, String toolingFixtures,
                                  String inspectionRequirement, Instant createdAt) {
        this.operationId = operationId;
        this.tenantId = tenantId;
        this.planId = planId;
        this.sequenceNumber = sequenceNumber;
        this.operationCode = operationCode;
        this.operationName = operationName;
        this.workCenterCode = workCenterCode;
        this.setupTimeMins = setupTimeMins;
        this.runTimeMins = runTimeMins;
        this.toolingFixtures = toolingFixtures;
        this.inspectionRequirement = inspectionRequirement;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long operationId;
        private String tenantId;
        private Long planId;
        private Integer sequenceNumber;
        private String operationCode;
        private String operationName;
        private String workCenterCode;
        private BigDecimal setupTimeMins = BigDecimal.ZERO;
        private BigDecimal runTimeMins = BigDecimal.ZERO;
        private String toolingFixtures;
        private String inspectionRequirement;
        private Instant createdAt = Instant.now();

        public Builder operationId(Long operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder planId(Long planId) {
            this.planId = planId;
            return this;
        }

        public Builder sequenceNumber(Integer sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder operationCode(String operationCode) {
            this.operationCode = operationCode;
            return this;
        }

        public Builder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public Builder workCenterCode(String workCenterCode) {
            this.workCenterCode = workCenterCode;
            return this;
        }

        public Builder setupTimeMins(BigDecimal setupTimeMins) {
            this.setupTimeMins = setupTimeMins;
            return this;
        }

        public Builder runTimeMins(BigDecimal runTimeMins) {
            this.runTimeMins = runTimeMins;
            return this;
        }

        public Builder toolingFixtures(String toolingFixtures) {
            this.toolingFixtures = toolingFixtures;
            return this;
        }

        public Builder inspectionRequirement(String inspectionRequirement) {
            this.inspectionRequirement = inspectionRequirement;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProcessOperationEntity build() {
            return new ProcessOperationEntity(operationId, tenantId, planId, sequenceNumber,
                    operationCode, operationName, workCenterCode, setupTimeMins,
                    runTimeMins, toolingFixtures, inspectionRequirement, createdAt);
        }
    }

    public Long getOperationId() {
        return operationId;
    }

    public void setOperationId(Long operationId) {
        this.operationId = operationId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getWorkCenterCode() {
        return workCenterCode;
    }

    public void setWorkCenterCode(String workCenterCode) {
        this.workCenterCode = workCenterCode;
    }

    public BigDecimal getSetupTimeMins() {
        return setupTimeMins;
    }

    public void setSetupTimeMins(BigDecimal setupTimeMins) {
        this.setupTimeMins = setupTimeMins;
    }

    public BigDecimal getRunTimeMins() {
        return runTimeMins;
    }

    public void setRunTimeMins(BigDecimal runTimeMins) {
        this.runTimeMins = runTimeMins;
    }

    public String getToolingFixtures() {
        return toolingFixtures;
    }

    public void setToolingFixtures(String toolingFixtures) {
        this.toolingFixtures = toolingFixtures;
    }

    public String getInspectionRequirement() {
        return inspectionRequirement;
    }

    public void setInspectionRequirement(String inspectionRequirement) {
        this.inspectionRequirement = inspectionRequirement;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
