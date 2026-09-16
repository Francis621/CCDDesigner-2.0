package com.ccdd.iam.dto;

import com.ccdd.iam.entity.ProjectRoleType;

import java.time.Instant;

/**
 * 分配用户至项目工作组请求 DTO (对齐 OpenAPI 6.1)
 */
public class AssignProjectMemberRequest {

    private String userId;
    private ProjectRoleType projectRole;
    private Instant effectiveFrom;
    private Instant effectiveTo;

    public AssignProjectMemberRequest() {
    }

    public AssignProjectMemberRequest(String userId, ProjectRoleType projectRole, Instant effectiveFrom, Instant effectiveTo) {
        this.userId = userId;
        this.projectRole = projectRole;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
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
}
