package com.ccdd.iam.dto;

import com.ccdd.iam.entity.DisciplineType;
import com.ccdd.iam.entity.SysQualificationEntity;
import com.ccdd.iam.entity.UserAccountStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户详情 DTO (包含所属部门、学科、全局角色与专职资质)
 */
public class UserDetailDto {

    private String userId;
    private Long deptId;
    private String deptCode;
    private String deptName;
    private DisciplineType disciplineType;
    private String username;
    private String realName;
    private String email;
    private String mobile;
    private UserAccountStatus status;
    private Boolean isExternal;
    private Instant lastLoginAt;
    private Instant createdAt;
    private List<String> roleIds = new ArrayList<>();
    private List<String> roleNames = new ArrayList<>();
    private List<SysQualificationEntity> qualifications = new ArrayList<>();

    public UserDetailDto() {
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

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public DisciplineType getDisciplineType() {
        return disciplineType;
    }

    public void setDisciplineType(DisciplineType disciplineType) {
        this.disciplineType = disciplineType;
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

    public List<String> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<String> roleIds) {
        this.roleIds = roleIds;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public List<SysQualificationEntity> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<SysQualificationEntity> qualifications) {
        this.qualifications = qualifications;
    }
}
