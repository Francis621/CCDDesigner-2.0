package com.ccdd.workflow.dto;

import java.io.Serializable;
import java.time.Instant;

/**
 * 待办审批任务条目 DTO (供前端工控待办卡片与表格展示)
 */
public class WorkflowTaskItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId;
    private String taskName;
    private Long workflowInstId;
    private String flowableProcInstId;
    private String targetObjectType;
    private Long targetObjectId;
    private String targetBusinessCode;
    private String projectId;
    private String initiatorId;
    private String assignee;
    private Boolean isSelfApprovalRestricted; // SoD 守卫标记：发起人是否被限制自审
    private Instant createdAt;

    public WorkflowTaskItemDto() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Long getWorkflowInstId() {
        return workflowInstId;
    }

    public void setWorkflowInstId(Long workflowInstId) {
        this.workflowInstId = workflowInstId;
    }

    public String getFlowableProcInstId() {
        return flowableProcInstId;
    }

    public void setFlowableProcInstId(String flowableProcInstId) {
        this.flowableProcInstId = flowableProcInstId;
    }

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public Long getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(Long targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public String getTargetBusinessCode() {
        return targetBusinessCode;
    }

    public void setTargetBusinessCode(String targetBusinessCode) {
        this.targetBusinessCode = targetBusinessCode;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Boolean getIsSelfApprovalRestricted() {
        return isSelfApprovalRestricted;
    }

    public void setIsSelfApprovalRestricted(Boolean selfApprovalRestricted) {
        isSelfApprovalRestricted = selfApprovalRestricted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
