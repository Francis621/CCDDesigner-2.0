package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 项目群容器实体 (Program)
 * 映射 plm_project.program
 * 铁律：项目群仅作为跨机型研制组合容器，物理与业务层严禁设立阶段门 Gate
 */
public class ProgramEntity {

    private Long programId;
    private String tenantId;
    private String programCode;
    private String name;
    private String description;
    private String managerId;
    private String status;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public ProgramEntity() {
    }

    public ProgramEntity(Long programId, String tenantId, String programCode, String name, String description,
                         String managerId, String status, String createdBy, Instant createdAt, Instant updatedAt) {
        this.programId = programId;
        this.tenantId = tenantId;
        this.programCode = programCode;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long programId;
        private String tenantId;
        private String programCode;
        private String name;
        private String description;
        private String managerId;
        private String status = "ACTIVE";
        private String createdBy;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder programId(Long programId) { this.programId = programId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder programCode(String programCode) { this.programCode = programCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder managerId(String managerId) { this.managerId = managerId; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProgramEntity build() {
            return new ProgramEntity(programId, tenantId, programCode, name, description, managerId, status, createdBy, createdAt, updatedAt);
        }
    }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getProgramCode() { return programCode; }
    public void setProgramCode(String programCode) { this.programCode = programCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
