package com.ccdd.workflow.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 启动审批流程请求 DTO (对齐 OpenAPI 8.1)
 */
public class StartWorkflowRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String targetObjectType;
    private Long targetObjectId;
    private String targetBusinessCode;
    private String targetContentHash;
    private String businessCategory;
    private String projectId;
    private String workflowTitle;
    private Map<String, Object> initialVariables = new HashMap<>();

    public StartWorkflowRequest() {
    }

    public String getTargetObjectType() {
        return targetObjectType;
    }

    public void setTargetObjectType(String targetObjectType) {
        this.targetObjectType = targetObjectType;
    }

    public Long getTargetObjectId() {
        return targetObjectId;
    }

    public void setTargetObjectId(Long targetObjectId) {
        this.targetObjectId = targetObjectId;
    }

    public String getTargetBusinessCode() {
        return targetBusinessCode;
    }

    public void setTargetBusinessCode(String targetBusinessCode) {
        this.targetBusinessCode = targetBusinessCode;
    }

    public String getTargetContentHash() {
        return targetContentHash;
    }

    public void setTargetContentHash(String targetContentHash) {
        this.targetContentHash = targetContentHash;
    }

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getWorkflowTitle() {
        return workflowTitle;
    }

    public void setWorkflowTitle(String workflowTitle) {
        this.workflowTitle = workflowTitle;
    }

    public Map<String, Object> getInitialVariables() {
        return initialVariables;
    }

    public void setInitialVariables(Map<String, Object> initialVariables) {
        this.initialVariables = initialVariables;
    }
}
