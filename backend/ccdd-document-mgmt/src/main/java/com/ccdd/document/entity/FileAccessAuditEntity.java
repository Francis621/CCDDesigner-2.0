package com.ccdd.document.entity;

import java.time.Instant;

/**
 * M19 文件物理访问与下载审计实体 (FileAccessAudit)
 */
public class FileAccessAuditEntity {

    private Long auditId;
    private String tenantId;
    private String userId;
    private Long artifactId;
    private Long revisionId;
    private String accessType; // PREVIEW, DOWNLOAD, EXPORT_PACKAGE
    private String clientIp;
    private String userAgent;
    private String authorizationTicket;
    private Long downloadBytes;
    private Instant accessedAt;

    public FileAccessAuditEntity() {
    }

    public FileAccessAuditEntity(Long auditId, String tenantId, String userId, Long artifactId,
                                Long revisionId, String accessType, String clientIp, String userAgent,
                                String authorizationTicket, Long downloadBytes, Instant accessedAt) {
        this.auditId = auditId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.artifactId = artifactId;
        this.revisionId = revisionId;
        this.accessType = accessType;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.authorizationTicket = authorizationTicket;
        this.downloadBytes = downloadBytes;
        this.accessedAt = accessedAt;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public Long getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(Long revisionId) {
        this.revisionId = revisionId;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientMachineIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAuthorizationTicket() {
        return authorizationTicket;
    }

    public void setAuthorizationTicket(String authorizationTicket) {
        this.authorizationTicket = authorizationTicket;
    }

    public Long getDownloadBytes() {
        return downloadBytes;
    }

    public void setDownloadBytes(Long downloadBytes) {
        this.downloadBytes = downloadBytes;
    }

    public Instant getAccessedAt() {
        return accessedAt;
    }

    public void setAccessedAt(Instant accessedAt) {
        this.accessedAt = accessedAt;
    }
}
