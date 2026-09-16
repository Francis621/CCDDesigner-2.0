package com.ccdd.message.dto;

import java.io.Serializable;

/**
 * 业务操作直达跳转响应 DTO
 * 携带目标路由、参数及安全上下文
 */
public class ResolveActionResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 目标路由/动作 URL（如 /change/cr-001 或 /task/gate-3）
     */
    private String targetActionUrl;

    /**
     * 关联工程对象类型
     */
    private String relatedObjType;

    /**
     * 关联工程对象业务主键
     */
    private String relatedObjId;

    /**
     * 关联机床型号或项目 ID
     */
    private String relatedProjectId;

    /**
     * 是否允许跳转访问
     */
    private Boolean allowed;

    /**
     * 提示或安全校验说明
     */
    private String message;

    public ResolveActionResponse() {
    }

    public ResolveActionResponse(String targetActionUrl, String relatedObjType, String relatedObjId, String relatedProjectId, Boolean allowed, String message) {
        this.targetActionUrl = targetActionUrl;
        this.relatedObjType = relatedObjType;
        this.relatedObjId = relatedObjId;
        this.relatedProjectId = relatedProjectId;
        this.allowed = allowed;
        this.message = message;
    }

    public String getTargetActionUrl() {
        return targetActionUrl;
    }

    public void setTargetActionUrl(String targetActionUrl) {
        this.targetActionUrl = targetActionUrl;
    }

    public String getRelatedObjType() {
        return relatedObjType;
    }

    public void setRelatedObjType(String relatedObjType) {
        this.relatedObjType = relatedObjType;
    }

    public String getRelatedObjId() {
        return relatedObjId;
    }

    public void setRelatedObjId(String relatedObjId) {
        this.relatedObjId = relatedObjId;
    }

    public String getRelatedProjectId() {
        return relatedProjectId;
    }

    public void setRelatedProjectId(String relatedProjectId) {
        this.relatedProjectId = relatedProjectId;
    }

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
