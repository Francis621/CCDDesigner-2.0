package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 机型研制工程项目实体 (Project)
 * 映射 plm_project.project
 * 承载研发模式 (PLATFORM / CTO / ETO)、Stage 集合与 WBS 树
 */
public class ProjectEntity {

    private Long projectId;
    private Long programId;
    private String tenantId;
    private String projectCode;
    private String name;
    private String projectType; // PLATFORM, CTO, ETO, PRE_RESEARCH
    private String managerId;
    private String chiefEngineerId;
    private Long currentStageId;
    private String status; // PLANNING, ACTIVE, SUSPENDED, COMPLETED, TERMINATED
    private Long workingVersion;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public ProjectEntity() {
    }

    public ProjectEntity(Long projectId, Long programId, String tenantId, String projectCode, String name,
                         String projectType, String managerId, String chiefEngineerId, Long currentStageId,
                         String status, Long workingVersion, String createdBy, Instant createdAt, Instant updatedAt) {
        this.projectId = projectId;
        this.programId = programId;
        this.tenantId = tenantId;
        this.projectCode = projectCode;
        this.name = name;
        this.projectType = projectType;
        this.managerId = managerId;
        this.chiefEngineerId = chiefEngineerId;
        this.currentStageId = currentStageId;
        this.status = status;
        this.workingVersion = workingVersion;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long projectId;
        private Long programId;
        private String tenantId;
        private String projectCode;
        private String name;
        private String projectType = "PLATFORM";
        private String managerId;
        private String chiefEngineerId;
        private Long currentStageId;
        private String status = "ACTIVE";
        private Long workingVersion = 1L;
        private String createdBy;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder programId(Long programId) { this.programId = programId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder projectCode(String projectCode) { this.projectCode = projectCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder projectType(String projectType) { this.projectType = projectType; return this; }
        public Builder managerId(String managerId) { this.managerId = managerId; return this; }
        public Builder chiefEngineerId(String chiefEngineerId) { this.chiefEngineerId = chiefEngineerId; return this; }
        public Builder currentStageId(Long currentStageId) { this.currentStageId = currentStageId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder workingVersion(Long workingVersion) { this.workingVersion = workingVersion; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProjectEntity build() {
            return new ProjectEntity(projectId, programId, tenantId, projectCode, name, projectType,
                    managerId, chiefEngineerId, currentStageId, status, workingVersion, createdBy, createdAt, updatedAt);
        }
    }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getChiefEngineerId() { return chiefEngineerId; }
    public void setChiefEngineerId(String chiefEngineerId) { this.chiefEngineerId = chiefEngineerId; }
    public Long getCurrentStageId() { return currentStageId; }
    public void setCurrentStageId(Long currentStageId) { this.currentStageId = currentStageId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getWorkingVersion() { return workingVersion; }
    public void setWorkingVersion(Long workingVersion) { this.workingVersion = workingVersion; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
