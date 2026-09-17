package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M04/M06: 系统模型工程项目主表 (SystemModelProject)
 */
public class SystemModelProject implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long modelProjectId;
    private Long projectId;
    private String modelCode;
    private String name;
    private String repositoryType = "FLEXO";
    private String modelLanguage = "SYSML_V2";
    private String defaultBranch = "main";
    private Long currentRevisionId;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public SystemModelProject() {}

    public SystemModelProject(Long modelProjectId, Long projectId, String modelCode, String name,
                              String repositoryType, String modelLanguage, String defaultBranch,
                              Long currentRevisionId, String createdBy, Instant createdAt, Instant updatedAt) {
        this.modelProjectId = modelProjectId;
        this.projectId = projectId;
        this.modelCode = modelCode;
        this.name = name;
        this.repositoryType = repositoryType;
        this.modelLanguage = modelLanguage;
        this.defaultBranch = defaultBranch;
        this.currentRevisionId = currentRevisionId;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getModelProjectId() { return modelProjectId; }
    public void setModelProjectId(Long modelProjectId) { this.modelProjectId = modelProjectId; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRepositoryType() { return repositoryType; }
    public void setRepositoryType(String repositoryType) { this.repositoryType = repositoryType; }

    public String getModelLanguage() { return modelLanguage; }
    public void setModelLanguage(String modelLanguage) { this.modelLanguage = modelLanguage; }

    public String getDefaultBranch() { return defaultBranch; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public Long getCurrentRevisionId() { return currentRevisionId; }
    public void setCurrentRevisionId(Long currentRevisionId) { this.currentRevisionId = currentRevisionId; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
