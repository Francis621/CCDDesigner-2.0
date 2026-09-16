package com.ccdd.manufacturing.dto;

import java.time.Instant;

/**
 * 制造工艺卡 (SOP) 与检验报告附件元数据 DTO
 * 映射 MinIO 对象存储制品元数据
 */
public class ProcessDocumentDto {

    private String documentId;
    private String tenantId;
    private String businessType; // OPERATION_SOP, INSPECTION_CERTIFICATE, ROUTING_OVERVIEW
    private String businessKey;  // 如 OP10, OP20, DISPATCH-20260916-01
    private String fileName;
    private String objectKey;
    private Long fileSizeBytes;
    private String sha256Digest;
    private String presignedDownloadUrl;
    private String uploader;
    private Instant createdAt;

    public ProcessDocumentDto() {
    }

    public ProcessDocumentDto(String documentId, String tenantId, String businessType, String businessKey,
                              String fileName, String objectKey, Long fileSizeBytes, String sha256Digest,
                              String presignedDownloadUrl, String uploader, Instant createdAt) {
        this.documentId = documentId;
        this.tenantId = tenantId;
        this.businessType = businessType;
        this.businessKey = businessKey;
        this.fileName = fileName;
        this.objectKey = objectKey;
        this.fileSizeBytes = fileSizeBytes;
        this.sha256Digest = sha256Digest;
        this.presignedDownloadUrl = presignedDownloadUrl;
        this.uploader = uploader;
        this.createdAt = createdAt;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getSha256Digest() {
        return sha256Digest;
    }

    public void setSha256Digest(String sha256Digest) {
        this.sha256Digest = sha256Digest;
    }

    public String getPresignedDownloadUrl() {
        return presignedDownloadUrl;
    }

    public void setPresignedDownloadUrl(String presignedDownloadUrl) {
        this.presignedDownloadUrl = presignedDownloadUrl;
    }

    public String getUploader() {
        return uploader;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
