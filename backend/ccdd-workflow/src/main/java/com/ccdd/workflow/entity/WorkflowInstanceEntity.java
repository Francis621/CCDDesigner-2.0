package com.ccdd.workflow.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * M24: PLM 流程实例快照实体 (桥接 Flowable 运行实例与业务实体)
 */
public class WorkflowInstanceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long workflowInstId;
    private String flowableProcInstId;
    private Long bindingId;
    private String targetObjectType;
    private Long targetObjectId;
    private String targetBusinessCode;
    private String targetContentHash;
    private String projectId;
    private String initiatorId;
    private InstanceStatus status;
    private ApprovalConclusion conclusion;
    private String terminationReason;
    private Instant startedAt;
    private Instant completedAt;

    public WorkflowInstanceEntity() {
    }

    public WorkflowInstanceEntity(Long workflowInstId, String flowableProcInstId, Long bindingId,
                                  String targetObjectType, Long targetObjectId, String targetBusinessCode,
                                  String targetContentHash, String projectId, String initiatorId,
                                  InstanceStatus status, ApprovalConclusion conclusion, String terminationReason,
                                  Instant startedAt, Instant completedAt) {
        this.workflowInstId = workflowInstId;
        this.flowableProcInstId = flowableProcInstId;
        this.bindingId = bindingId;
        this.targetObjectType = targetObjectType;
        this.targetObjectId = targetObjectId;
        this.targetBusinessCode = targetBusinessCode;
        this.targetContentHash = targetContentHash;
        this.projectId = projectId;
        this.initiatorId = initiatorId;
        this.status = status;
        this.conclusion = conclusion;
        this.terminationReason = terminationReason;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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

    public Long getBindingId() {
        return bindingId;
    }

    public void setBindingId(Long bindingId) {
        this.bindingId = bindingId;
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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(String initiatorId) {
        this.initiatorId = initiatorId;
    }

    public InstanceStatus getStatus() {
        return status;
    }

    public void setStatus(InstanceStatus status) {
        this.status = status;
    }

    public ApprovalConclusion getConclusion() {
        return conclusion;
    }

    public void setConclusion(ApprovalConclusion conclusion) {
        this.conclusion = conclusion;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
