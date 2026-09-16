package com.ccdd.iam.entity;

import java.time.Instant;

/**
 * 企业部门与专业学科实体
 */
public class SysDepartmentEntity {

    private Long deptId;
    private String deptCode;
    private String deptName;
    private Long parentDeptId;
    private DisciplineType disciplineType;
    private Instant createdAt;

    public SysDepartmentEntity() {
    }

    public SysDepartmentEntity(Long deptId, String deptCode, String deptName, Long parentDeptId, DisciplineType disciplineType, Instant createdAt) {
        this.deptId = deptId;
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.parentDeptId = parentDeptId;
        this.disciplineType = disciplineType;
        this.createdAt = createdAt;
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

    public Long getParentDeptId() {
        return parentDeptId;
    }

    public void setParentDeptId(Long parentDeptId) {
        this.parentDeptId = parentDeptId;
    }

    public DisciplineType getDisciplineType() {
        return disciplineType;
    }

    public void setDisciplineType(DisciplineType disciplineType) {
        this.disciplineType = disciplineType;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "SysDepartmentEntity{" +
                "deptId=" + deptId +
                ", deptCode='" + deptCode + '\'' +
                ", deptName='" + deptName + '\'' +
                ", disciplineType=" + disciplineType +
                '}';
    }
}
