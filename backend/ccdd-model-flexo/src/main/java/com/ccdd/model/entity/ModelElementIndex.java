package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

public class ModelElementIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long elementIndexId;
    private String tenantId;
    private Long releaseId;
    private String repositoryId;
    private String modelProjectId;
    private String commitId;
    private String elementId;
    private String elementName;
    private String elementType;
    private String displayPath;
    private String elementUri;
    private String elementHash;
    private String attributesJson;
    private Instant createdAt;

    public ModelElementIndex() {
    }

    public ModelElementIndex(Long elementIndexId, String tenantId, Long releaseId, String repositoryId,
                             String modelProjectId, String commitId, String elementId, String elementName,
                             String elementType, String displayPath, String elementUri, String elementHash,
                             String attributesJson, Instant createdAt) {
        this.elementIndexId = elementIndexId;
        this.tenantId = tenantId;
        this.releaseId = releaseId;
        this.repositoryId = repositoryId;
        this.modelProjectId = modelProjectId;
        this.commitId = commitId;
        this.elementId = elementId;
        this.elementName = elementName;
        this.elementType = elementType;
        this.displayPath = displayPath;
        this.elementUri = elementUri;
        this.elementHash = elementHash;
        this.attributesJson = attributesJson;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long elementIndexId;
        private String tenantId;
        private Long releaseId;
        private String repositoryId;
        private String modelProjectId;
        private String commitId;
        private String elementId;
        private String elementName;
        private String elementType;
        private String displayPath;
        private String elementUri;
        private String elementHash;
        private String attributesJson;
        private Instant createdAt;

        public Builder elementIndexId(Long elementIndexId) { this.elementIndexId = elementIndexId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder releaseId(Long releaseId) { this.releaseId = releaseId; return this; }
        public Builder repositoryId(String repositoryId) { this.repositoryId = repositoryId; return this; }
        public Builder modelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; return this; }
        public Builder commitId(String commitId) { this.commitId = commitId; return this; }
        public Builder elementId(String elementId) { this.elementId = elementId; return this; }
        public Builder elementName(String elementName) { this.elementName = elementName; return this; }
        public Builder elementType(String elementType) { this.elementType = elementType; return this; }
        public Builder displayPath(String displayPath) { this.displayPath = displayPath; return this; }
        public Builder elementUri(String elementUri) { this.elementUri = elementUri; return this; }
        public Builder elementHash(String elementHash) { this.elementHash = elementHash; return this; }
        public Builder attributesJson(String attributesJson) { this.attributesJson = attributesJson; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ModelElementIndex build() {
            return new ModelElementIndex(elementIndexId, tenantId, releaseId, repositoryId, modelProjectId,
                    commitId, elementId, elementName, elementType, displayPath, elementUri,
                    elementHash, attributesJson, createdAt);
        }
    }

    public Long getElementIndexId() { return elementIndexId; }
    public void setElementIndexId(Long elementIndexId) { this.elementIndexId = elementIndexId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }

    public String getRepositoryId() { return repositoryId; }
    public void setRepositoryId(String repositoryId) { this.repositoryId = repositoryId; }

    public String getModelProjectId() { return modelProjectId; }
    public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }

    public String getCommitId() { return commitId; }
    public void setCommitId(String commitId) { this.commitId = commitId; }

    public String getElementId() { return elementId; }
    public void setElementId(String elementId) { this.elementId = elementId; }

    public String getElementName() { return elementName; }
    public void setElementName(String elementName) { this.elementName = elementName; }

    public String getElementType() { return elementType; }
    public void setElementType(String elementType) { this.elementType = elementType; }

    public String getDisplayPath() { return displayPath; }
    public void setDisplayPath(String displayPath) { this.displayPath = displayPath; }

    public String getElementUri() { return elementUri; }
    public void setElementUri(String elementUri) { this.elementUri = elementUri; }

    public String getElementHash() { return elementHash; }
    public void setElementHash(String elementHash) { this.elementHash = elementHash; }

    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
