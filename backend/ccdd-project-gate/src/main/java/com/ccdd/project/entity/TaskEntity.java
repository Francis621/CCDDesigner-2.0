package com.ccdd.project.entity;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 任务执行活动实体 (Task)
 * 映射 plm_project.task
 * 核心原则：显式外键关联 stage_id (实现计划与阶段正交)，进度百分比 (0~100%) 与交付物状态彻底解耦
 */
public class TaskEntity {

    private Long taskId;
    private Long wbsNodeId;
    private Long stageId;
    private String taskCode;
    private String name;
    private String assigneeId;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Integer durationDays;
    private Integer progressPercent;
    private TaskStatus status;
    private Long workingVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public TaskEntity() {
    }

    public TaskEntity(Long taskId, Long wbsNodeId, Long stageId, String taskCode, String name,
                      String assigneeId, LocalDate plannedStartDate, LocalDate plannedEndDate,
                      LocalDate actualStartDate, LocalDate actualEndDate, Integer durationDays,
                      Integer progressPercent, TaskStatus status, Long workingVersion,
                      Instant createdAt, Instant updatedAt) {
        this.taskId = taskId;
        this.wbsNodeId = wbsNodeId;
        this.stageId = stageId;
        this.taskCode = taskCode;
        this.name = name;
        this.assigneeId = assigneeId;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
        this.durationDays = durationDays;
        this.progressPercent = progressPercent;
        this.status = status;
        this.workingVersion = workingVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long taskId;
        private Long wbsNodeId;
        private Long stageId;
        private String taskCode;
        private String name;
        private String assigneeId;
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private Integer durationDays = 1;
        private Integer progressPercent = 0;
        private TaskStatus status = TaskStatus.NOT_STARTED;
        private Long workingVersion = 1L;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder taskId(Long taskId) { this.taskId = taskId; return this; }
        public Builder wbsNodeId(Long wbsNodeId) { this.wbsNodeId = wbsNodeId; return this; }
        public Builder stageId(Long stageId) { this.stageId = stageId; return this; }
        public Builder taskCode(String taskCode) { this.taskCode = taskCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder assigneeId(String assigneeId) { this.assigneeId = assigneeId; return this; }
        public Builder plannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; return this; }
        public Builder plannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; return this; }
        public Builder actualStartDate(LocalDate actualStartDate) { this.actualStartDate = actualStartDate; return this; }
        public Builder actualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; return this; }
        public Builder durationDays(Integer durationDays) { this.durationDays = durationDays; return this; }
        public Builder progressPercent(Integer progressPercent) { this.progressPercent = progressPercent; return this; }
        public Builder status(TaskStatus status) { this.status = status; return this; }
        public Builder workingVersion(Long workingVersion) { this.workingVersion = workingVersion; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public TaskEntity build() {
            return new TaskEntity(taskId, wbsNodeId, stageId, taskCode, name, assigneeId,
                    plannedStartDate, plannedEndDate, actualStartDate, actualEndDate,
                    durationDays, progressPercent, status, workingVersion, createdAt, updatedAt);
        }
    }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getWbsNodeId() { return wbsNodeId; }
    public void setWbsNodeId(Long wbsNodeId) { this.wbsNodeId = wbsNodeId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getTaskCode() { return taskCode; }
    public void setTaskCode(String taskCode) { this.taskCode = taskCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAssigneeId() { return assigneeId; }
    public void setAssigneeId(String assigneeId) { this.assigneeId = assigneeId; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public void setPlannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDate actualStartDate) { this.actualStartDate = actualStartDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }
    public Integer getDurationDays() { return durationDays; }
    public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Long getWorkingVersion() { return workingVersion; }
    public void setWorkingVersion(Long workingVersion) { this.workingVersion = workingVersion; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
