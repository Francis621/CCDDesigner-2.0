package com.ccdd.change.entity;

import java.time.Instant;

/**
 * M22 工程变更请求实体 (ChangeRequest - ECR)
 * 界定问题起因与初始变更诉求，负责“为什么变”与“是否值得变”
 */
public class ChangeRequestEntity {

    private Long ecrId;
    private String tenantId;
    private Long projectId;
    private String ecrNumber;
    private String title;
    private ChangeReasonType reasonType;
    private String problemDescription;
    private String proposedSolution;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, EMERGENCY
    private EcrStatus status;
    private Long sourceServiceCaseId;
    private String originatorId;
    private Instant createdAt;
    private Instant updatedAt;

    public ChangeRequestEntity() {
    }

    public ChangeRequestEntity(Long ecrId, String tenantId, Long projectId, String ecrNumber,
                               String title, ChangeReasonType reasonType, String problemDescription,
                               String proposedSolution, String urgencyLevel, EcrStatus status,
                               Long sourceServiceCaseId, String originatorId, Instant createdAt, Instant updatedAt) {
        this.ecrId = ecrId;
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.ecrNumber = ecrNumber;
        this.title = title;
        this.reasonType = reasonType;
        this.problemDescription = problemDescription;
        this.proposedSolution = proposedSolution;
        this.urgencyLevel = urgencyLevel;
        this.status = status;
        this.sourceServiceCaseId = sourceServiceCaseId;
        this.originatorId = originatorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getEcrNumber() {
        return ecrNumber;
    }

    public void setEcrNumber(String ecrNumber) {
        this.ecrNumber = ecrNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ChangeReasonType getReasonType() {
        return reasonType;
    }

    public void setReasonType(ChangeReasonType reasonType) {
        this.reasonType = reasonType;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getProposedSolution() {
        return proposedSolution;
    }

    public void setProposedSolution(String proposedSolution) {
        this.proposedSolution = proposedSolution;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public EcrStatus getStatus() {
        return status;
    }

    public void setStatus(EcrStatus status) {
        this.status = status;
    }

    public Long getSourceServiceCaseId() {
        return sourceServiceCaseId;
    }

    public void setSourceServiceCaseId(Long sourceServiceCaseId) {
        this.sourceServiceCaseId = sourceServiceCaseId;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
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
