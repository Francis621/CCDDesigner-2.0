package com.ccdd.change.dto;

import com.ccdd.change.entity.ChangeReasonType;

/**
 * 创建变更请求 (ECR) 请求 DTO
 */
public class CreateEcrRequest {

    private Long projectId;
    private String ecrNumber;
    private String title;
    private ChangeReasonType reasonType;
    private String problemDescription;
    private String proposedSolution;
    private String urgencyLevel; // LOW, MEDIUM, HIGH, EMERGENCY
    private Long sourceServiceCaseId;
    private String originatorId;

    public CreateEcrRequest() {
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
}
