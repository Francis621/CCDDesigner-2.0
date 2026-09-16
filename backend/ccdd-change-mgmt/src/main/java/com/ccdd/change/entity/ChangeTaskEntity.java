package com.ccdd.change.entity;

import java.time.Instant;

/**
 * M22 变更实施分解任务实体 (ChangeTask - 驱动设计与验证工作)
 */
public class ChangeTaskEntity {

    private Long taskId;
    private Long ecoId;
    private String taskCode;
    private String title;
    private String taskType; // CAD_REMODEL, SIM_RERUN, REQ_UPDATE, DRAWING_REDRAW
    private String assigneeId;
    private Long sourceRevisionId;
    private Long targetRevisionId; // 产生的新修订 DRAFT
    private String status; // PENDING, IN_PROGRESS, COMPLETED
    private Instant completedAt;

    public ChangeTaskEntity() {
    }

    public ChangeTaskEntity(Long taskId, Long ecoId, String taskCode, String title, String taskType,
                            String assigneeId, Long sourceRevisionId, Long targetRevisionId,
                            String status, Instant completedAt) {
        this.taskId = taskId;
        this.ecoId = ecoId;
        this.taskCode = taskCode;
        this.title = title;
        this.taskType = taskType;
        this.assigneeId = assigneeId;
        this.sourceRevisionId = sourceRevisionId;
        this.targetRevisionId = targetRevisionId;
        this.status = status;
        this.completedAt = completedAt;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getEcoId() {
        return ecoId;
    }

    public void setEcoId(Long ecoId) {
        this.ecoId = ecoId;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(String assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getSourceRevisionId() {
        return sourceRevisionId;
    }

    public void setSourceRevisionId(Long sourceRevisionId) {
        this.sourceRevisionId = sourceRevisionId;
    }

    public Long getTargetRevisionId() {
        return targetRevisionId;
    }

    public void setTargetRevisionId(Long targetRevisionId) {
        this.targetRevisionId = targetRevisionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
