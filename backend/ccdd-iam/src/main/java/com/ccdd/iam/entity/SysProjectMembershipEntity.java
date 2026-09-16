package com.ccdd.iam.entity;

import java.time.Instant;

/**
 * 项目工作组成员与项目级角色实体 (细粒度工程授权核心)
 */
public class SysProjectMembershipEntity {

    private Long membershipId;
    private Long projectId;
    private String userId;
    private ProjectRoleType projectRole;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Boolean isActive;
    private String grantedBy;

    public SysProjectMembershipEntity() {
    }

    public SysProjectMembershipEntity(Long membershipId, Long projectId, String userId, ProjectRoleType projectRole,
                                      Instant effectiveFrom, Instant effectiveTo, Boolean isActive, String grantedBy) {
        this.membershipId = membershipId;
        this.projectId = projectId;
        this.userId = userId;
        this.projectRole = projectRole;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive;
        this.grantedBy = grantedBy;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ProjectRoleType getProjectRole() {
        return projectRole;
    }

    public void setProjectRole(ProjectRoleType projectRole) {
        this.projectRole = projectRole;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    @Override
    public String toString() {
        return "SysProjectMembershipEntity{" +
                "membershipId=" + membershipId +
                ", projectId=" + projectId +
                ", userId='" + userId + '\'' +
                ", projectRole=" + projectRole +
                ", isActive=" + isActive +
                '}';
    }
}
