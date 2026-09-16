package com.ccdd.iam.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户主实体
 */
public class SysUserEntity {

    private String userId;
    private Long deptId;
    private String username;
    private String realName;
    private String email;
    private String mobile;
    private UserAccountStatus status;
    private Boolean isExternal;
    private String ssoSub;
    private String passwordHash;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * 关联的全局职能角色列表
     */
    private List<String> roleIds = new ArrayList<>();

    public SysUserEntity() {
    }

    public SysUserEntity(String userId, Long deptId, String username, String realName, String email, String mobile,
                         UserAccountStatus status, Boolean isExternal, String ssoSub, String passwordHash,
                         Instant lastLoginAt, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.deptId = deptId;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.mobile = mobile;
        this.status = status;
        this.isExternal = isExternal;
        this.ssoSub = ssoSub;
        this.passwordHash = passwordHash;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public UserAccountStatus getStatus() {
        return status;
    }

    public void setStatus(UserAccountStatus status) {
        this.status = status;
    }

    public Boolean getIsExternal() {
        return isExternal;
    }

    public void setIsExternal(Boolean external) {
        isExternal = external;
    }

    public String getSsoSub() {
        return ssoSub;
    }

    public void setSsoSub(String ssoSub) {
        this.ssoSub = ssoSub;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    @Override
    public String toString() {
        return "SysUserEntity{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", status=" + status +
                '}';
    }
}
