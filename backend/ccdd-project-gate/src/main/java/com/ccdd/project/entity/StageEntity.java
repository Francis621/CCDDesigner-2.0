package com.ccdd.project.entity;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 研发业务阶段定义实体 (Stage)
 * 映射 plm_project.stage
 */
public class StageEntity {

    private Long stageId;
    private Long projectId;
    private String stageCode;
    private String name;
    private Integer sequenceNo;
    private String status; // PENDING, IN_PROGRESS, IN_GATE_REVIEW, CLOSED
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private Instant createdAt;

    public StageEntity() {
    }

    public StageEntity(Long stageId, Long projectId, String stageCode, String name, Integer sequenceNo,
                       String status, LocalDate plannedStartDate, LocalDate plannedEndDate,
                       LocalDate actualStartDate, LocalDate actualEndDate, Instant createdAt) {
        this.stageId = stageId;
        this.projectId = projectId;
        this.stageCode = stageCode;
        this.name = name;
        this.sequenceNo = sequenceNo;
        this.status = status;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.actualStartDate = actualStartDate;
        this.actualEndDate = actualEndDate;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long stageId;
        private Long projectId;
        private String stageCode;
        private String name;
        private Integer sequenceNo;
        private String status = "PENDING";
        private LocalDate plannedStartDate;
        private LocalDate plannedEndDate;
        private LocalDate actualStartDate;
        private LocalDate actualEndDate;
        private Instant createdAt = Instant.now();

        public Builder stageId(Long stageId) { this.stageId = stageId; return this; }
        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder stageCode(String stageCode) { this.stageCode = stageCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder plannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; return this; }
        public Builder plannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; return this; }
        public Builder actualStartDate(LocalDate actualStartDate) { this.actualStartDate = actualStartDate; return this; }
        public Builder actualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public StageEntity build() {
            return new StageEntity(stageId, projectId, stageCode, name, sequenceNo, status,
                    plannedStartDate, plannedEndDate, actualStartDate, actualEndDate, createdAt);
        }
    }

    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getStageCode() { return stageCode; }
    public void setStageCode(String stageCode) { this.stageCode = stageCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getPlannedStartDate() { return plannedStartDate; }
    public void setPlannedStartDate(LocalDate plannedStartDate) { this.plannedStartDate = plannedStartDate; }
    public LocalDate getPlannedEndDate() { return plannedEndDate; }
    public void setPlannedEndDate(LocalDate plannedEndDate) { this.plannedEndDate = plannedEndDate; }
    public LocalDate getActualStartDate() { return actualStartDate; }
    public void setActualStartDate(LocalDate actualStartDate) { this.actualStartDate = actualStartDate; }
    public LocalDate getActualEndDate() { return actualEndDate; }
    public void setActualEndDate(LocalDate actualEndDate) { this.actualEndDate = actualEndDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
