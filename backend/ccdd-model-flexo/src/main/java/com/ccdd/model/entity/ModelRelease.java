package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

public class ModelRelease implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long releaseId;
    private String tenantId;
    private Long bindingId;
    private Long snapshotId;
    private Long profileId;
    private String modelProjectId;
    private String releaseVersion;

    /** 业务生命周期状态: DRAFT, IN_REVIEW, RELEASED, OBSOLETE, WITHDRAWN */
    private String lifecycleState;

    /** 两阶段执行协调状态: CAPTURING, VALIDATING, STAGING, IN_REVIEW, FINALIZING, RELEASED, FAILED */
    private String executionState;
    private String failedStep;
    private String failedReason;

    private String flexoRepositoryId;
    private String flexoStagingGraph;
    private String flexoCommitId;
    private String artifactBundleSha256;
    private String artifactBundleUri;
    private String combinedReleaseHash;

    private String workflowInstanceId;
    private String approvalDecisionId;
    private String approvedBy;
    private Instant approvedAt;

    private String publishedBy;
    private Instant publishedAt;
    private Long workingVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public ModelRelease() {
    }

    public ModelRelease(Long releaseId, String tenantId, Long bindingId, Long snapshotId, Long profileId,
                        String modelProjectId, String releaseVersion, String lifecycleState,
                        String executionState, String failedStep, String failedReason,
                        String flexoRepositoryId, String flexoStagingGraph, String flexoCommitId,
                        String artifactBundleSha256, String artifactBundleUri, String combinedReleaseHash,
                        String workflowInstanceId, String approvalDecisionId, String approvedBy,
                        Instant approvedAt, String publishedBy, Instant publishedAt, Long workingVersion,
                        Instant createdAt, Instant updatedAt) {
        this.releaseId = releaseId;
        this.tenantId = tenantId;
        this.bindingId = bindingId;
        this.snapshotId = snapshotId;
        this.profileId = profileId;
        this.modelProjectId = modelProjectId;
        this.releaseVersion = releaseVersion;
        this.lifecycleState = lifecycleState;
        this.executionState = executionState;
        this.failedStep = failedStep;
        this.failedReason = failedReason;
        this.flexoRepositoryId = flexoRepositoryId;
        this.flexoStagingGraph = flexoStagingGraph;
        this.flexoCommitId = flexoCommitId;
        this.artifactBundleSha256 = artifactBundleSha256;
        this.artifactBundleUri = artifactBundleUri;
        this.combinedReleaseHash = combinedReleaseHash;
        this.workflowInstanceId = workflowInstanceId;
        this.approvalDecisionId = approvalDecisionId;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
        this.workingVersion = workingVersion;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long releaseId;
        private String tenantId;
        private Long bindingId;
        private Long snapshotId;
        private Long profileId;
        private String modelProjectId;
        private String releaseVersion;
        private String lifecycleState;
        private String executionState;
        private String failedStep;
        private String failedReason;
        private String flexoRepositoryId;
        private String flexoStagingGraph;
        private String flexoCommitId;
        private String artifactBundleSha256;
        private String artifactBundleUri;
        private String combinedReleaseHash;
        private String workflowInstanceId;
        private String approvalDecisionId;
        private String approvedBy;
        private Instant approvedAt;
        private String publishedBy;
        private Instant publishedAt;
        private Long workingVersion;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder releaseId(Long releaseId) { this.releaseId = releaseId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder bindingId(Long bindingId) { this.bindingId = bindingId; return this; }
        public Builder snapshotId(Long snapshotId) { this.snapshotId = snapshotId; return this; }
        public Builder profileId(Long profileId) { this.profileId = profileId; return this; }
        public Builder modelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; return this; }
        public Builder releaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; return this; }
        public Builder lifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; return this; }
        public Builder executionState(String executionState) { this.executionState = executionState; return this; }
        public Builder failedStep(String failedStep) { this.failedStep = failedStep; return this; }
        public Builder failedReason(String failedReason) { this.failedReason = failedReason; return this; }
        public Builder flexoRepositoryId(String flexoRepositoryId) { this.flexoRepositoryId = flexoRepositoryId; return this; }
        public Builder flexoStagingGraph(String flexoStagingGraph) { this.flexoStagingGraph = flexoStagingGraph; return this; }
        public Builder flexoCommitId(String flexoCommitId) { this.flexoCommitId = flexoCommitId; return this; }
        public Builder artifactBundleSha256(String artifactBundleSha256) { this.artifactBundleSha256 = artifactBundleSha256; return this; }
        public Builder artifactBundleUri(String artifactBundleUri) { this.artifactBundleUri = artifactBundleUri; return this; }
        public Builder combinedReleaseHash(String combinedReleaseHash) { this.combinedReleaseHash = combinedReleaseHash; return this; }
        public Builder workflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; return this; }
        public Builder approvalDecisionId(String approvalDecisionId) { this.approvalDecisionId = approvalDecisionId; return this; }
        public Builder approvedBy(String approvedBy) { this.approvedBy = approvedBy; return this; }
        public Builder approvedAt(Instant approvedAt) { this.approvedAt = approvedAt; return this; }
        public Builder publishedBy(String publishedBy) { this.publishedBy = publishedBy; return this; }
        public Builder publishedAt(Instant publishedAt) { this.publishedAt = publishedAt; return this; }
        public Builder workingVersion(Long workingVersion) { this.workingVersion = workingVersion; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public ModelRelease build() {
            return new ModelRelease(releaseId, tenantId, bindingId, snapshotId, profileId,
                    modelProjectId, releaseVersion, lifecycleState, executionState,
                    failedStep, failedReason, flexoRepositoryId, flexoStagingGraph,
                    flexoCommitId, artifactBundleSha256, artifactBundleUri,
                    combinedReleaseHash, workflowInstanceId, approvalDecisionId,
                    approvedBy, approvedAt, publishedBy, publishedAt, workingVersion,
                    createdAt, updatedAt);
        }
    }

    public Long getReleaseId() { return releaseId; }
    public void setReleaseId(Long releaseId) { this.releaseId = releaseId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

    public Long getSnapshotId() { return snapshotId; }
    public void setSnapshotId(Long snapshotId) { this.snapshotId = snapshotId; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public String getModelProjectId() { return modelProjectId; }
    public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }

    public String getReleaseVersion() { return releaseVersion; }
    public void setReleaseVersion(String releaseVersion) { this.releaseVersion = releaseVersion; }

    public String getLifecycleState() { return lifecycleState; }
    public void setLifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; }

    public String getExecutionState() { return executionState; }
    public void setExecutionState(String executionState) { this.executionState = executionState; }

    public String getFailedStep() { return failedStep; }
    public void setFailedStep(String failedStep) { this.failedStep = failedStep; }

    public String getFailedReason() { return failedReason; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }

    public String getFlexoRepositoryId() { return flexoRepositoryId; }
    public void setFlexoRepositoryId(String flexoRepositoryId) { this.flexoRepositoryId = flexoRepositoryId; }

    public String getFlexoStagingGraph() { return flexoStagingGraph; }
    public void setFlexoStagingGraph(String flexoStagingGraph) { this.flexoStagingGraph = flexoStagingGraph; }

    public String getFlexoCommitId() { return flexoCommitId; }
    public void setFlexoCommitId(String flexoCommitId) { this.flexoCommitId = flexoCommitId; }

    public String getArtifactBundleSha256() { return artifactBundleSha256; }
    public void setArtifactBundleSha256(String artifactBundleSha256) { this.artifactBundleSha256 = artifactBundleSha256; }

    public String getArtifactBundleUri() { return artifactBundleUri; }
    public void setArtifactBundleUri(String artifactBundleUri) { this.artifactBundleUri = artifactBundleUri; }

    public String getCombinedReleaseHash() { return combinedReleaseHash; }
    public void setCombinedReleaseHash(String combinedReleaseHash) { this.combinedReleaseHash = combinedReleaseHash; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public String getApprovalDecisionId() { return approvalDecisionId; }
    public void setApprovalDecisionId(String approvalDecisionId) { this.approvalDecisionId = approvalDecisionId; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }

    public Long getWorkingVersion() { return workingVersion; }
    public void setWorkingVersion(Long workingVersion) { this.workingVersion = workingVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
