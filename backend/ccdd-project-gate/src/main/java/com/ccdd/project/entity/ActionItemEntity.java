package com.ccdd.project.entity;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 阶段门整改行动项实体 (ActionItem)
 * 映射 plm_project.action_item
 * 铁律：处于 OPEN/RESOLVED 未闭环状态的行动项将硬阻断后续基线冻结与阶段门 PASS
 */
public class ActionItemEntity {

    private Long actionItemId;
    private Long decisionId;
    private String title;
    private String description;
    private String ownerId;
    private String approverId;
    private LocalDate dueDate;
    private String status; // OPEN, RESOLVED, CLOSED, OVERDUE
    private String resolutionSummary;
    private Instant closedAt;
    private Instant createdAt;

    public ActionItemEntity() {
    }

    public ActionItemEntity(Long actionItemId, Long decisionId, String title, String description,
                            String ownerId, String approverId, LocalDate dueDate, String status,
                            String resolutionSummary, Instant closedAt, Instant createdAt) {
        this.actionItemId = actionItemId;
        this.decisionId = decisionId;
        this.title = title;
        this.description = description;
        this.ownerId = ownerId;
        this.approverId = approverId;
        this.dueDate = dueDate;
        this.status = status;
        this.resolutionSummary = resolutionSummary;
        this.closedAt = closedAt;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long actionItemId;
        private Long decisionId;
        private String title;
        private String description;
        private String ownerId;
        private String approverId;
        private LocalDate dueDate;
        private String status = "OPEN";
        private String resolutionSummary;
        private Instant closedAt;
        private Instant createdAt = Instant.now();

        public Builder actionItemId(Long actionItemId) { this.actionItemId = actionItemId; return this; }
        public Builder decisionId(Long decisionId) { this.decisionId = decisionId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder ownerId(String ownerId) { this.ownerId = ownerId; return this; }
        public Builder approverId(String approverId) { this.approverId = approverId; return this; }
        public Builder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder resolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; return this; }
        public Builder closedAt(Instant closedAt) { this.closedAt = closedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ActionItemEntity build() {
            return new ActionItemEntity(actionItemId, decisionId, title, description, ownerId, approverId,
                    dueDate, status, resolutionSummary, closedAt, createdAt);
        }
    }

    public Long getActionItemId() { return actionItemId; }
    public void setActionItemId(Long actionItemId) { this.actionItemId = actionItemId; }
    public Long getDecisionId() { return decisionId; }
    public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public String getApproverId() { return approverId; }
    public void setApproverId(String approverId) { this.approverId = approverId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolutionSummary() { return resolutionSummary; }
    public void setResolutionSummary(String resolutionSummary) { this.resolutionSummary = resolutionSummary; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
