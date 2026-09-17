package com.ccdd.workflow.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M24: 人工审批流水与审查意见追踪实体
 */
public class WorkflowActionRecordEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long actionRecordId;
    private Long workflowInstId;
    private String flowableTaskId;
    private String taskNodeName;
    private String operatorId;
    private String actionType; // APPROVE, REJECT, DELEGATE, ADD_SIGN, REVOKE
    private String commentText;
    private Long attachmentArtifactId;
    private String delegatedToUserId;
    private Instant votedAt;

    public WorkflowActionRecordEntity() {
    }

    public WorkflowActionRecordEntity(Long actionRecordId, Long workflowInstId, String flowableTaskId,
                                      String taskNodeName, String operatorId, String actionType,
                                      String commentText, Long attachmentArtifactId, String delegatedToUserId,
                                      Instant votedAt) {
        this.actionRecordId = actionRecordId;
        this.workflowInstId = workflowInstId;
        this.flowableTaskId = flowableTaskId;
        this.taskNodeName = taskNodeName;
        this.operatorId = operatorId;
        this.actionType = actionType;
        this.commentText = commentText;
        this.attachmentArtifactId = attachmentArtifactId;
        this.delegatedToUserId = delegatedToUserId;
        this.votedAt = votedAt;
    }

    public Long getActionRecordId() {
        return actionRecordId;
    }

    public void setActionRecordId(Long actionRecordId) {
        this.actionRecordId = actionRecordId;
    }

    public Long getWorkflowInstId() {
        return workflowInstId;
    }

    public void setWorkflowInstId(Long workflowInstId) {
        this.workflowInstId = workflowInstId;
    }

    public String getFlowableTaskId() {
        return flowableTaskId;
    }

    public void setFlowableTaskId(String flowableTaskId) {
        this.flowableTaskId = flowableTaskId;
    }

    public String getTaskNodeName() {
        return taskNodeName;
    }

    public void setTaskNodeName(String taskNodeName) {
        this.taskNodeName = taskNodeName;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Long getAttachmentArtifactId() {
        return attachmentArtifactId;
    }

    public void setAttachmentArtifactId(Long attachmentArtifactId) {
        this.attachmentArtifactId = attachmentArtifactId;
    }

    public String getDelegatedToUserId() {
        return delegatedToUserId;
    }

    public void setDelegatedToUserId(String delegatedToUserId) {
        this.delegatedToUserId = delegatedToUserId;
    }

    public Instant getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Instant votedAt) {
        this.votedAt = votedAt;
    }
}
