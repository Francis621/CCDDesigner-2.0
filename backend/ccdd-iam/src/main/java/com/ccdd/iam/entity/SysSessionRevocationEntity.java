package com.ccdd.iam.entity;

import java.time.Instant;

/**
 * 权限即时撤销黑名单实体 (支撑 AT-13 即时失效)
 */
public class SysSessionRevocationEntity {

    private Long revocationId;
    private String userId;
    private Long projectId; // 若为特定项目移除则填充，全局封禁则为空
    private Instant revokedBefore; // 早于该时点的有效凭证全量熔断
    private String reason;
    private Instant createdAt;

    public SysSessionRevocationEntity() {
    }

    public SysSessionRevocationEntity(Long revocationId, String userId, Long projectId, Instant revokedBefore, String reason, Instant createdAt) {
        this.revocationId = revocationId;
        this.userId = userId;
        this.projectId = projectId;
        this.revokedBefore = revokedBefore;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public Long getRevocationId() {
        return revocationId;
    }

    public void setRevocationId(Long revocationId) {
        this.revocationId = revocationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Instant getRevokedBefore() {
        return revokedBefore;
    }

    public void setRevokedBefore(Instant revokedBefore) {
        this.revokedBefore = revokedBefore;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SysSessionRevocationEntity{" +
                "revocationId=" + revocationId +
                ", userId='" + userId + '\'' +
                ", projectId=" + projectId +
                ", revokedBefore=" + revokedBefore +
                ", reason='" + reason + '\'' +
                '}';
    }
}
