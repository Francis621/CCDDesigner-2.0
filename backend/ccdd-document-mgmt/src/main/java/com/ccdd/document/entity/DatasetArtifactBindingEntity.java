package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 数据集与物理制品受控绑定关系实体 (DatasetArtifactBinding)
 */
public class DatasetArtifactBindingEntity {

    private Long bindingId;
    private Long datasetId;
    private Long artifactId;
    private DatasetRole fileRole;
    private Boolean isCurrent;
    private Instant boundAt;
    private String boundBy;

    public DatasetArtifactBindingEntity() {
    }

    public DatasetArtifactBindingEntity(Long bindingId, Long datasetId, Long artifactId,
                                        DatasetRole fileRole, Boolean isCurrent, Instant boundAt, String boundBy) {
        this.bindingId = bindingId;
        this.datasetId = datasetId;
        this.artifactId = artifactId;
        this.fileRole = fileRole;
        this.isCurrent = isCurrent;
        this.boundAt = boundAt;
        this.boundBy = boundBy;
    }

    public Long getBindingId() {
        return bindingId;
    }

    public void setBindingId(Long bindingId) {
        this.bindingId = bindingId;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public DatasetRole getFileRole() {
        return fileRole;
    }

    public void setFileRole(DatasetRole fileRole) {
        this.fileRole = fileRole;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean current) {
        isCurrent = current;
    }

    public Instant getBoundAt() {
        return boundAt;
    }

    public void setBoundAt(Instant boundAt) {
        this.boundAt = boundAt;
    }

    public String getBoundBy() {
        return boundBy;
    }

    public void setBoundBy(String boundBy) {
        this.boundBy = boundBy;
    }
}
