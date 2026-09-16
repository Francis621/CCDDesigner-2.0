package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 物理文件制品元数据实体 (Artifact - 核心防篡改不可变实体)
 */
public class ArtifactEntity {

    private Long artifactId;
    private String tenantId;
    private String storageBucket;
    private String storageObjectPath;
    private String fileName;
    private String fileExtension;
    private Long fileSizeBytes;
    private String mimeType;
    private String sha256Hash;
    private String etag;
    private Boolean isQuarantined;
    private String uploadedBy;
    private Instant uploadedAt;

    public ArtifactEntity() {
    }

    public ArtifactEntity(Long artifactId, String tenantId, String storageBucket, String storageObjectPath,
                          String fileName, String fileExtension, Long fileSizeBytes, String mimeType,
                          String sha256Hash, String etag, Boolean isQuarantined, String uploadedBy, Instant uploadedAt) {
        this.artifactId = artifactId;
        this.tenantId = tenantId;
        this.storageBucket = storageBucket;
        this.storageObjectPath = storageObjectPath;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.sha256Hash = sha256Hash;
        this.etag = etag;
        this.isQuarantined = isQuarantined;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = uploadedAt;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getStorageBucket() {
        return storageBucket;
    }

    public void setStorageBucket(String storageBucket) {
        this.storageBucket = storageBucket;
    }

    public String getStorageObjectPath() {
        return storageObjectPath;
    }

    public void setStorageObjectPath(String storageObjectPath) {
        this.storageObjectPath = storageObjectPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public Boolean getIsQuarantined() {
        return isQuarantined;
    }

    public void setIsQuarantined(Boolean quarantined) {
        isQuarantined = quarantined;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
