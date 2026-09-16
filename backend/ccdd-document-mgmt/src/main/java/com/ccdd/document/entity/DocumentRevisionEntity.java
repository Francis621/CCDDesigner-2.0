package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 文档受控工程修订版本实体 (Revision - 承载生命周期与协同状态)
 */
public class DocumentRevisionEntity {

    private Long revisionId;
    private Long masterId;
    private String revisionLabel; // 如 A.1, B.0
    private String lifecycleState; // DRAFT, IN_REVIEW, RELEASED, OBSOLETE, ARCHIVED
    private SecurityClassification securityLevel;
    private Integer pageCount;
    private String cadSoftwareType; // SolidWorks, NX, CATIA, AutoCAD, EPLAN
    private String cadSoftwareVersion;
    private Boolean isTemplate;
    private String summary;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public DocumentRevisionEntity() {
    }

    public DocumentRevisionEntity(Long revisionId, Long masterId, String revisionLabel, String lifecycleState,
                                  SecurityClassification securityLevel, Integer pageCount, String cadSoftwareType,
                                  String cadSoftwareVersion, Boolean isTemplate, String summary, String createdBy,
                                  Instant createdAt, Instant updatedAt) {
        this.revisionId = revisionId;
        this.masterId = masterId;
        this.revisionLabel = revisionLabel;
        this.lifecycleState = lifecycleState;
        this.securityLevel = securityLevel;
        this.pageCount = pageCount;
        this.cadSoftwareType = cadSoftwareType;
        this.cadSoftwareVersion = cadSoftwareVersion;
        this.isTemplate = isTemplate;
        this.summary = summary;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public Long getMasterId() {
        return masterId;
    }

    public void setMasterId(Long masterId) {
        this.masterId = masterId;
    }

    public String getRevisionLabel() {
        return revisionLabel;
    }

    public void setRevisionLabel(String revisionLabel) {
        this.revisionLabel = revisionLabel;
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public SecurityClassification getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityClassification securityLevel) {
        this.securityLevel = securityLevel;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getCadSoftwareType() {
        return cadSoftwareType;
    }

    public void setCadSoftwareType(String cadSoftwareType) {
        this.cadSoftwareType = cadSoftwareType;
    }

    public String getCadSoftwareVersion() {
        return cadSoftwareVersion;
    }

    public void setCadSoftwareVersion(String cadSoftwareVersion) {
        this.cadSoftwareVersion = cadSoftwareVersion;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean template) {
        isTemplate = template;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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
