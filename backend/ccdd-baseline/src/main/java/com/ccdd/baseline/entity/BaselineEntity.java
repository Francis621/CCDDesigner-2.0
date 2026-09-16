package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 工程基线主表实体 (Baseline - FROZEN 后物理级不可篡改)
 */
public class BaselineEntity {

    private Long baselineId;
    private Long projectId;
    private String tenantId;
    private String baselineCode;
    private String name;
    private BaselinePurpose purpose;
    private BaselineState state;
    private String description;
    private String closureHash; // 全闭包节点与拓扑关系 SHA-256 强校验摘要 (64位)
    private Long workingVersion;
    private String createdBy;
    private Instant createdAt;
    private String frozenBy;
    private Instant frozenAt;
    private Long approvalTicketId;

    public BaselineEntity() {
    }

    public BaselineEntity(Long baselineId, Long projectId, String tenantId, String baselineCode,
                          String name, BaselinePurpose purpose, BaselineState state, String description,
                          String closureHash, Long workingVersion, String createdBy, Instant createdAt,
                          String frozenBy, Instant frozenAt, Long approvalTicketId) {
        this.baselineId = baselineId;
        this.projectId = projectId;
        this.tenantId = tenantId;
        this.baselineCode = baselineCode;
        this.name = name;
        this.purpose = purpose;
        this.state = state;
        this.description = description;
        this.closureHash = closureHash;
        this.workingVersion = workingVersion;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.frozenBy = frozenBy;
        this.frozenAt = frozenAt;
        this.approvalTicketId = approvalTicketId;
    }

    public boolean isFrozen() {
        return state == BaselineState.FROZEN || state == BaselineState.SUPERSEDED;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBaselineCode() {
        return baselineCode;
    }

    public void setBaselineCode(String baselineCode) {
        this.baselineCode = baselineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BaselinePurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(BaselinePurpose purpose) {
        this.purpose = purpose;
    }

    public BaselineState getState() {
        return state;
    }

    public void setState(BaselineState state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClosureHash() {
        return closureHash;
    }

    public void setClosureHash(String closureHash) {
        this.closureHash = closureHash;
    }

    public Long getWorkingVersion() {
        return workingVersion;
    }

    public void setWorkingVersion(Long workingVersion) {
        this.workingVersion = workingVersion;
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

    public String getFrozenBy() {
        return frozenBy;
    }

    public void setFrozenBy(String frozenBy) {
        this.frozenBy = frozenBy;
    }

    public Instant getFrozenAt() {
        return frozenAt;
    }

    public void setFrozenAt(Instant frozenAt) {
        this.frozenAt = frozenAt;
    }

    public Long getApprovalTicketId() {
        return approvalTicketId;
    }

    public void setApprovalTicketId(Long approvalTicketId) {
        this.approvalTicketId = approvalTicketId;
    }
}
