package com.ccdd.manufacturing.entity;

import java.time.Instant;

/**
 * 制造下发批次包实体
 * 映射物理表 sys_handoff_packages
 */
public class HandoffPackageEntity {

    private Long packageId;
    private String tenantId;
    private String handoffBatchNo;
    private Long mbomRevisionId;
    private String targetSystem;
    private String packageDigestSha256;
    private HandoffExecutionState executionState;
    private Integer totalLineCount;
    private Integer acceptedLineCount;
    private Integer rejectedLineCount;
    private String createdBy;
    private Instant createdAt;
    private Instant reconciledAt;

    public HandoffPackageEntity() {
    }

    public HandoffPackageEntity(Long packageId, String tenantId, String handoffBatchNo, Long mbomRevisionId,
                                String targetSystem, String packageDigestSha256, HandoffExecutionState executionState,
                                Integer totalLineCount, Integer acceptedLineCount, Integer rejectedLineCount,
                                String createdBy, Instant createdAt, Instant reconciledAt) {
        this.packageId = packageId;
        this.tenantId = tenantId;
        this.handoffBatchNo = handoffBatchNo;
        this.mbomRevisionId = mbomRevisionId;
        this.targetSystem = targetSystem;
        this.packageDigestSha256 = packageDigestSha256;
        this.executionState = executionState;
        this.totalLineCount = totalLineCount;
        this.acceptedLineCount = acceptedLineCount;
        this.rejectedLineCount = rejectedLineCount;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.reconciledAt = reconciledAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long packageId;
        private String tenantId;
        private String handoffBatchNo;
        private Long mbomRevisionId;
        private String targetSystem = "MES_PLANT_01";
        private String packageDigestSha256;
        private HandoffExecutionState executionState = HandoffExecutionState.DRAFT;
        private Integer totalLineCount = 0;
        private Integer acceptedLineCount = 0;
        private Integer rejectedLineCount = 0;
        private String createdBy;
        private Instant createdAt = Instant.now();
        private Instant reconciledAt;

        public Builder packageId(Long packageId) {
            this.packageId = packageId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder handoffBatchNo(String handoffBatchNo) {
            this.handoffBatchNo = handoffBatchNo;
            return this;
        }

        public Builder mbomRevisionId(Long mbomRevisionId) {
            this.mbomRevisionId = mbomRevisionId;
            return this;
        }

        public Builder targetSystem(String targetSystem) {
            this.targetSystem = targetSystem;
            return this;
        }

        public Builder packageDigestSha256(String packageDigestSha256) {
            this.packageDigestSha256 = packageDigestSha256;
            return this;
        }

        public Builder executionState(HandoffExecutionState executionState) {
            this.executionState = executionState;
            return this;
        }

        public Builder totalLineCount(Integer totalLineCount) {
            this.totalLineCount = totalLineCount;
            return this;
        }

        public Builder acceptedLineCount(Integer acceptedLineCount) {
            this.acceptedLineCount = acceptedLineCount;
            return this;
        }

        public Builder rejectedLineCount(Integer rejectedLineCount) {
            this.rejectedLineCount = rejectedLineCount;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder reconciledAt(Instant reconciledAt) {
            this.reconciledAt = reconciledAt;
            return this;
        }

        public HandoffPackageEntity build() {
            return new HandoffPackageEntity(packageId, tenantId, handoffBatchNo, mbomRevisionId,
                    targetSystem, packageDigestSha256, executionState, totalLineCount,
                    acceptedLineCount, rejectedLineCount, createdBy, createdAt, reconciledAt);
        }
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getHandoffBatchNo() {
        return handoffBatchNo;
    }

    public void setHandoffBatchNo(String handoffBatchNo) {
        this.handoffBatchNo = handoffBatchNo;
    }

    public Long getMbomRevisionId() {
        return mbomRevisionId;
    }

    public void setMbomRevisionId(Long mbomRevisionId) {
        this.mbomRevisionId = mbomRevisionId;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    public String getPackageDigestSha256() {
        return packageDigestSha256;
    }

    public void setPackageDigestSha256(String packageDigestSha256) {
        this.packageDigestSha256 = packageDigestSha256;
    }

    public HandoffExecutionState getExecutionState() {
        return executionState;
    }

    public void setExecutionState(HandoffExecutionState executionState) {
        this.executionState = executionState;
    }

    public Integer getTotalLineCount() {
        return totalLineCount;
    }

    public void setTotalLineCount(Integer totalLineCount) {
        this.totalLineCount = totalLineCount;
    }

    public Integer getAcceptedLineCount() {
        return acceptedLineCount;
    }

    public void setAcceptedLineCount(Integer acceptedLineCount) {
        this.acceptedLineCount = acceptedLineCount;
    }

    public Integer getRejectedLineCount() {
        return rejectedLineCount;
    }

    public void setRejectedLineCount(Integer rejectedLineCount) {
        this.rejectedLineCount = rejectedLineCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReconciledAt() {
        return reconciledAt;
    }

    public void setReconciledAt(Instant reconciledAt) {
        this.reconciledAt = reconciledAt;
    }
}
