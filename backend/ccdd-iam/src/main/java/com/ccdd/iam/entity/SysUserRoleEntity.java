package com.ccdd.iam.entity;

import java.time.Instant;

/**
 * 用户-全局角色关联实体
 */
public class SysUserRoleEntity {

    private String userId;
    private String roleId;
    private Instant assignedAt;
    private String assignedBy;

    public SysUserRoleEntity() {
    }

    public SysUserRoleEntity(String userId, String roleId, Instant assignedAt, String assignedBy) {
        this.userId = userId;
        this.roleId = roleId;
        this.assignedAt = assignedAt;
        this.assignedBy = assignedBy;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    @Override
    public String toString() {
        return "SysUserRoleEntity{" +
                "userId='" + userId + '\'' +
                ", roleId='" + roleId + '\'' +
                '}';
    }
}
