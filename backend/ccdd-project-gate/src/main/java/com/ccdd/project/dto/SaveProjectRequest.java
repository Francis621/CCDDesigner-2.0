package com.ccdd.project.dto;

/**
 * 项目创建与编辑请求 DTO
 */
public class SaveProjectRequest {

    private String projectCode;
    private String name;
    private String projectType; // PLATFORM / DERIVATIVE
    private String managerId;
    private String chiefEngineerId;
    private String description;

    public SaveProjectRequest() {
    }

    public SaveProjectRequest(String projectCode, String name, String projectType,
                              String managerId, String chiefEngineerId, String description) {
        this.projectCode = projectCode;
        this.name = name;
        this.projectType = projectType;
        this.managerId = managerId;
        this.chiefEngineerId = chiefEngineerId;
        this.description = description;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }

    public String getChiefEngineerId() {
        return chiefEngineerId;
    }

    public void setChiefEngineerId(String chiefEngineerId) {
        this.chiefEngineerId = chiefEngineerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
