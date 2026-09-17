package com.ccdd.workflow.dto;

import com.ccdd.workflow.entity.InstanceStatus;

import java.io.Serializable;
import java.time.Instant;

/**
 * 启动审批流程响应 DTO (对齐 OpenAPI 8.1)
 */
public class StartWorkflowResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long workflowInstId;
    private String flowableProcInstId;
    private InstanceStatus status;
    private Long bindingId;
    private String targetContentHash;
    private Instant startedAt;

    public StartWorkflowResponse() {
    }

    public StartWorkflowResponse(Long workflowInstId, String flowableProcInstId, InstanceStatus status,
                                 Long bindingId, String targetContentHash, Instant startedAt) {
        this.workflowInstId = workflowInstId;
        this.flowableProcInstId = flowableProcInstId;
        this.status = status;
        this.bindingId = bindingId;
        this.targetContentHash = targetContentHash;
        this.startedAt = startedAt;
    }

    public Long getWorkflowInstId() {
        return workflowInstId;
    }

    public void setWorkflowInstId(Long workflowInstId) {
        this.workflowInstId = workflowInstId;
    }

    public String getFlowableProcInstId() {
        return flowableProcInstId;
    }

    public void setFlowableProcInstId(String flowableProcInstId) {
        this.flowableProcInstId = flowableProcInstId;
    }

    public InstanceStatus getStatus() {
        return status;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
    }

    public Long getBindingId() {
        return bindingId;
    }

    public void setBindingId(Long bindingId) {
        this.bindingId = bindingId;
    }

    public String getTargetContentHash() {
        return targetContentHash;
    }

    public void setTargetContentHash(String targetContentHash) {
        this.targetContentHash = targetContentHash;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }
}
