package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 数据集逻辑容器实体 (Dataset - 文件业务逻辑容器)
 */
public class DatasetEntity {

    private Long datasetId;
    private Long revisionId;
    private String datasetCode;
    private String name;
    private Instant createdAt;

    public DatasetEntity() {
    }

    public DatasetEntity(Long datasetId, Long revisionId, String datasetCode, String name, Instant createdAt) {
        this.datasetId = datasetId;
        this.revisionId = revisionId;
        this.datasetCode = datasetCode;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public String getDatasetCode() {
        return datasetCode;
    }

    public void setDatasetCode(String datasetCode) {
        this.datasetCode = datasetCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
