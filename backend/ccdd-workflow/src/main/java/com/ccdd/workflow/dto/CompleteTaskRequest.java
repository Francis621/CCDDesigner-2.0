package com.ccdd.workflow.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 执行节点任务审批请求 DTO (对齐 OpenAPI 8.2)
 */
public class CompleteTaskRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 审批动作: APPROVE, REJECT, DELEGATE, ADD_SIGN
     */
    private String action = "APPROVE";

    /**
     * 审查意见文本
     */
    private String comment;

    /**
     * 关联的批注文件或图纸附件 (M19)
     */
    private Long attachmentArtifactId;

    /**
     * 委派受托人工号 (转办/委派场景)
     */
    private String delegatedToUserId;

    /**
     * 附加业务流程变量
     */
    private Map<String, Object> taskVariables = new HashMap<>();

    public CompleteTaskRequest() {
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Long getAttachmentArtifactId() {
        return attachmentArtifactId;
    }

    public void setAttachmentArtifactId(Long attachmentArtifactId) {
        this.attachmentArtifactId = attachmentArtifactId;
    }

    public String getDelegatedToUserId() {
        return delegatedToUserId;
    }

    public void setDelegatedToUserId(String delegatedToUserId) {
        this.delegatedToUserId = delegatedToUserId;
    }

    public Map<String, Object> getTaskVariables() {
        return taskVariables;
    }

    public void setTaskVariables(Map<String, Object> taskVariables) {
        this.taskVariables = taskVariables;
    }
}
