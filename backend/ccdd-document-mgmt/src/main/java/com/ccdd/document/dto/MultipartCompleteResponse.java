package com.ccdd.document.dto;

import java.time.Instant;

/**
 * 分片合并完成与登记响应报文 (OpenAPI §8.2)
 */
public class MultipartCompleteResponse {

    private Long artifactId;
    private String fileName;
    private String sha256Hash;
    private Long fileSizeBytes;
    private String status; // REGISTERED
    private Boolean verified;
    private Instant registeredAt;

    public MultipartCompleteResponse() {
    }

    public MultipartCompleteResponse(Long artifactId, String fileName, String sha256Hash,
                                     Long fileSizeBytes, String status, Boolean verified, Instant registeredAt) {
        this.artifactId = artifactId;
        this.fileName = fileName;
        this.sha256Hash = sha256Hash;
        this.fileSizeBytes = fileSizeBytes;
        this.status = status;
        this.verified = verified;
        this.registeredAt = registeredAt;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSha256Hash() {
        return sha256Hash;
    }

    public void setSha256Hash(String sha256Hash) {
        this.sha256Hash = sha256Hash;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public void setFileSizeBytes(Long fileSizeBytes) {
        this.fileSizeBytes = fileSizeBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }
}
