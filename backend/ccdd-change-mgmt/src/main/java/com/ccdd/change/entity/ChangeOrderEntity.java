package com.ccdd.change.entity;

import java.time.Instant;

/**
 * M22 工程变更实施单实体 (ChangeOrder - ECO)
 * 管理跨学科工程设计更新与现场生效，负责“如何受控实施”
 */
public class ChangeOrderEntity {

    private Long ecoId;
    private Long ecrId;
    private String tenantId;
    private String ecoNumber;
    private String title;
    private String changeCategory; // MAJOR, MINOR, ADMINISTRATIVE
    private Long targetBaselineId;
    private EcoStatus status;
    private Boolean isImpactAnalysisTruncated;
    private Long workingVersion;
    private Long ccbApprovalTicketId;
    private Instant releasedAt;
    private Instant closedAt;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public ChangeOrderEntity() {
    }

    public ChangeOrderEntity(Long ecoId, Long ecrId, String tenantId, String ecoNumber, String title,
                             String changeCategory, Long targetBaselineId, EcoStatus status,
                             Boolean isImpactAnalysisTruncated, Long workingVersion,
                             Long ccbApprovalTicketId, Instant releasedAt, Instant closedAt,
                             String createdBy, Instant createdAt, Instant updatedAt) {
        this.ecoId = ecoId;
        this.ecrId = ecrId;
        this.tenantId = tenantId;
        this.ecoNumber = ecoNumber;
        this.title = title;
        this.changeCategory = changeCategory;
        this.targetBaselineId = targetBaselineId;
        this.status = status;
        this.isImpactAnalysisTruncated = isImpactAnalysisTruncated;
        this.workingVersion = workingVersion;
        this.ccbApprovalTicketId = ccbApprovalTicketId;
        this.releasedAt = releasedAt;
        this.closedAt = closedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public Long getEcrId() {
        return ecrId;
    }

    public void setEcrId(Long ecrId) {
        this.ecrId = ecrId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getEcoNumber() {
        return ecoNumber;
    }

    public void setEcoNumber(String ecoNumber) {
        this.ecoNumber = ecoNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChangeCategory() {
        return changeCategory;
    }

    public void setChangeCategory(String changeCategory) {
        this.changeCategory = changeCategory;
    }

    public Long getTargetBaselineId() {
        return targetBaselineId;
    }

    public void setTargetBaselineId(Long targetBaselineId) {
        this.targetBaselineId = targetBaselineId;
    }

    public EcoStatus getStatus() {
        return status;
    }

    public void setStatus(EcoStatus status) {
        this.status = status;
    }

    public Boolean getIsImpactAnalysisTruncated() {
        return isImpactAnalysisTruncated;
    }

    public void setIsImpactAnalysisTruncated(Boolean impactAnalysisTruncated) {
        isImpactAnalysisTruncated = impactAnalysisTruncated;
    }

    public Long getWorkingVersion() {
        return workingVersion;
    }

    public void setWorkingVersion(Long workingVersion) {
        this.workingVersion = workingVersion;
    }

    public Long getCcbApprovalTicketId() {
        return ccbApprovalTicketId;
    }

    public void setCcbApprovalTicketId(Long ccbApprovalTicketId) {
        this.ccbApprovalTicketId = ccbApprovalTicketId;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Instant releasedAt) {
        this.releasedAt = releasedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
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

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
