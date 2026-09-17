package com.ccdd.workflow.dto;

import com.ccdd.workflow.entity.ApprovalConclusion;

import java.io.Serializable;
import java.time.Instant;

/**
 * 节点任务审批完成响应 DTO (对齐 OpenAPI 8.2)
 */
public class CompleteTaskResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId;
    private String action;
    private Boolean isProcessCompleted;
    private Long decisionTicketId;
    private ApprovalConclusion finalConclusion;
    private Instant completedAt;

    public CompleteTaskResponse() {
    }

    public CompleteTaskResponse(String taskId, String action, Boolean isProcessCompleted,
                                Long decisionTicketId, ApprovalConclusion finalConclusion, Instant completedAt) {
        this.taskId = taskId;
        this.action = action;
        this.isProcessCompleted = isProcessCompleted;
        this.decisionTicketId = decisionTicketId;
        this.finalConclusion = finalConclusion;
        this.completedAt = completedAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Boolean getIsProcessCompleted() {
        return isProcessCompleted;
    }

    public void setIsProcessCompleted(Boolean processCompleted) {
        isProcessCompleted = processCompleted;
    }

    public Long getDecisionTicketId() {
        return decisionTicketId;
    }

    public void setDecisionTicketId(Long decisionTicketId) {
        this.decisionTicketId = decisionTicketId;
    }

    public ApprovalConclusion getFinalConclusion() {
        return finalConclusion;
    }

    public void setFinalConclusion(ApprovalConclusion finalConclusion) {
        this.finalConclusion = finalConclusion;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
