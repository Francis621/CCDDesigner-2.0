package com.ccdd.workflow.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M24: 状态机异步回调幂等控制实体
 */
public class CallbackIdempotencyEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idempotencyKey;
    private Long workflowInstId;
    private String callbackType;
    private String executionStatus; // PROCESSING, SUCCESS, FAILED
    private String errorMessage;
    private Instant processedAt;

    public CallbackIdempotencyEntity() {
    }

    public CallbackIdempotencyEntity(String idempotencyKey, Long workflowInstId, String callbackType,
                                     String executionStatus, String errorMessage, Instant processedAt) {
        this.idempotencyKey = idempotencyKey;
        this.workflowInstId = workflowInstId;
        this.callbackType = callbackType;
        this.executionStatus = executionStatus;
        this.errorMessage = errorMessage;
        this.processedAt = processedAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Long getWorkflowInstId() {
        return workflowInstId;
    }

    public void setWorkflowInstId(Long workflowInstId) {
        this.workflowInstId = workflowInstId;
    }

    public String getCallbackType() {
        return callbackType;
    }

    public void setCallbackType(String callbackType) {
        this.callbackType = callbackType;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}
