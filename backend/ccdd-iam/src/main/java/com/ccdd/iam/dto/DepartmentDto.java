package com.ccdd.iam.dto;

import com.ccdd.iam.entity.DisciplineType;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门与专业学科 DTO
 */
public class DepartmentDto {

    private Long deptId;
    private String deptCode;
    private String deptName;
    private Long parentDeptId;
    private DisciplineType disciplineType;
    private String disciplineName;
    private Integer userCount = 0;
    private List<DepartmentDto> children = new ArrayList<>();

    public DepartmentDto() {
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
        if (disciplineType != null) {
            this.disciplineName = disciplineType.getDescription();
        }
    }

    public String getDisciplineName() {
        return disciplineName;
    }

    public void setDisciplineName(String disciplineName) {
        this.disciplineName = disciplineName;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public List<DepartmentDto> getChildren() {
        return children;
    }

    public void setChildren(List<DepartmentDto> children) {
        this.children = children;
    }
}
