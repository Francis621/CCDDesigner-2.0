package com.ccdd.manufacturing.entity;

import java.time.Instant;

/**
 * 制造 MBOM 修订版实体
 * 映射物理表 sys_manufacturing_bom_revisions
 */
public class ManufacturingBomRevisionEntity {

    private Long revisionId;
    private String tenantId;
    private Long mbomId;
    private String revisionVersion;
    private Long sourceEbomRevId;
    private String lifecycleState;
    private Boolean isBalanceVerified;
    private String balanceReportJson;
    private String publishedBy;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public ManufacturingBomRevisionEntity() {
    }

    public ManufacturingBomRevisionEntity(Long revisionId, String tenantId, Long mbomId, String revisionVersion,
                                         Long sourceEbomRevId, String lifecycleState, Boolean isBalanceVerified,
                                         String balanceReportJson, String publishedBy, Instant publishedAt,
                                         Instant createdAt, Instant updatedAt) {
        this.revisionId = revisionId;
        this.tenantId = tenantId;
        this.mbomId = mbomId;
        this.revisionVersion = revisionVersion;
        this.sourceEbomRevId = sourceEbomRevId;
        this.lifecycleState = lifecycleState;
        this.isBalanceVerified = isBalanceVerified;
        this.balanceReportJson = balanceReportJson;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long revisionId;
        private String tenantId;
        private Long mbomId;
        private String revisionVersion;
        private Long sourceEbomRevId;
        private String lifecycleState = "DRAFT";
        private Boolean isBalanceVerified = false;
        private String balanceReportJson;
        private String publishedBy;
        private Instant publishedAt;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder revisionId(Long revisionId) {
            this.revisionId = revisionId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder mbomId(Long mbomId) {
            this.mbomId = mbomId;
            return this;
        }

        public Builder revisionVersion(String revisionVersion) {
            this.revisionVersion = revisionVersion;
            return this;
        }

        public Builder sourceEbomRevId(Long sourceEbomRevId) {
            this.sourceEbomRevId = sourceEbomRevId;
            return this;
        }

        public Builder lifecycleState(String lifecycleState) {
            this.lifecycleState = lifecycleState;
            return this;
        }

        public Builder isBalanceVerified(Boolean isBalanceVerified) {
            this.isBalanceVerified = isBalanceVerified;
            return this;
        }

        public Builder balanceReportJson(String balanceReportJson) {
            this.balanceReportJson = balanceReportJson;
            return this;
        }

        public Builder publishedBy(String publishedBy) {
            this.publishedBy = publishedBy;
            return this;
        }

        public Builder publishedAt(Instant publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ManufacturingBomRevisionEntity build() {
            return new ManufacturingBomRevisionEntity(revisionId, tenantId, mbomId, revisionVersion,
                    sourceEbomRevId, lifecycleState, isBalanceVerified, balanceReportJson,
                    publishedBy, publishedAt, createdAt, updatedAt);
        }
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getMbomId() {
        return mbomId;
    }

    public void setMbomId(Long mbomId) {
        this.mbomId = mbomId;
    }

    public String getRevisionVersion() {
        return revisionVersion;
    }

    public void setRevisionVersion(String revisionVersion) {
        this.revisionVersion = revisionVersion;
    }

    public Long getSourceEbomRevId() {
        return sourceEbomRevId;
    }

    public void setSourceEbomRevId(Long sourceEbomRevId) {
        this.sourceEbomRevId = sourceEbomRevId;
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public Boolean getIsBalanceVerified() {
        return isBalanceVerified;
    }

    public void setIsBalanceVerified(Boolean balanceVerified) {
        isBalanceVerified = balanceVerified;
    }

    public String getBalanceReportJson() {
        return balanceReportJson;
    }

    public void setBalanceReportJson(String balanceReportJson) {
        this.balanceReportJson = balanceReportJson;
    }

    public String getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(String publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
