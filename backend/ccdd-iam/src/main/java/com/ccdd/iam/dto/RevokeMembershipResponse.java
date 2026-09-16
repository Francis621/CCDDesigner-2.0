package com.ccdd.iam.dto;

import java.time.Instant;

/**
 * 撤销用户项目成员权限响应 DTO (对齐 OpenAPI 6.2 与 AT-13 熔断)
 */
public class RevokeMembershipResponse {

    private String userId;
    private Long projectId;
    private Boolean revoked;
    private Instant revokedAt;
    private Boolean sessionBlacklistPushed;
    private Boolean minioPresignedRevoked;

    public RevokeMembershipResponse() {
    }

    public RevokeMembershipResponse(String userId, Long projectId, Boolean revoked, Instant revokedAt,
                                  Boolean sessionBlacklistPushed, Boolean minioPresignedRevoked) {
        this.userId = userId;
        this.projectId = projectId;
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.sessionBlacklistPushed = sessionBlacklistPushed;
        this.minioPresignedRevoked = minioPresignedRevoked;
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

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }

    public Boolean getSessionBlacklistPushed() {
        return sessionBlacklistPushed;
    }

    public void setSessionBlacklistPushed(Boolean sessionBlacklistPushed) {
        this.sessionBlacklistPushed = sessionBlacklistPushed;
    }

    public Boolean getMinioPresignedRevoked() {
        return minioPresignedRevoked;
    }

    public void setMinioPresignedRevoked(Boolean minioPresignedRevoked) {
        this.minioPresignedRevoked = minioPresignedRevoked;
    }
}
