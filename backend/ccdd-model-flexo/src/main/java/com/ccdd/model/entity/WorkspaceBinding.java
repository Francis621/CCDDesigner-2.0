package com.ccdd.model.entity;

import java.io.Serializable;
import java.time.Instant;

public class WorkspaceBinding implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long bindingId;
    private String tenantId;
    private String projectId;
    private String modelProjectId;
    private String modelProjectName;
    
    /** 主编辑通道: GRAPHICAL, TEXTUAL */
    private String primaryChannel;
    private String channelLockToken;
    private Instant channelLockExpiresAt;
    
    /** 工作区状态: ACTIVE, LOCKED, RELEASING, ARCHIVED */
    private String currentWorkspaceState;
    private String boundUserId;
    private String sysonProjectUri;
    private String baseCommitId;
    
    private Instant createdAt;
    private Instant updatedAt;

    public WorkspaceBinding() {
    }

    public WorkspaceBinding(Long bindingId, String tenantId, String projectId, String modelProjectId,
                            String modelProjectName, String primaryChannel, String channelLockToken,
                            Instant channelLockExpiresAt, String currentWorkspaceState, String boundUserId,
                            String sysonProjectUri, String baseCommitId, Instant createdAt, Instant updatedAt) {
        this.bindingId = bindingId;
        this.tenantId = tenantId;
        this.projectId = projectId;
        this.modelProjectId = modelProjectId;
        this.modelProjectName = modelProjectName;
        this.primaryChannel = primaryChannel;
        this.channelLockToken = channelLockToken;
        this.channelLockExpiresAt = channelLockExpiresAt;
        this.currentWorkspaceState = currentWorkspaceState;
        this.boundUserId = boundUserId;
        this.sysonProjectUri = sysonProjectUri;
        this.baseCommitId = baseCommitId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long bindingId;
        private String tenantId;
        private String projectId;
        private String modelProjectId;
        private String modelProjectName;
        private String primaryChannel;
        private String channelLockToken;
        private Instant channelLockExpiresAt;
        private String currentWorkspaceState;
        private String boundUserId;
        private String sysonProjectUri;
        private String baseCommitId;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder bindingId(Long bindingId) { this.bindingId = bindingId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder modelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; return this; }
        public Builder modelProjectName(String modelProjectName) { this.modelProjectName = modelProjectName; return this; }
        public Builder primaryChannel(String primaryChannel) { this.primaryChannel = primaryChannel; return this; }
        public Builder channelLockToken(String channelLockToken) { this.channelLockToken = channelLockToken; return this; }
        public Builder channelLockExpiresAt(Instant channelLockExpiresAt) { this.channelLockExpiresAt = channelLockExpiresAt; return this; }
        public Builder currentWorkspaceState(String currentWorkspaceState) { this.currentWorkspaceState = currentWorkspaceState; return this; }
        public Builder boundUserId(String boundUserId) { this.boundUserId = boundUserId; return this; }
        public Builder sysonProjectUri(String sysonProjectUri) { this.sysonProjectUri = sysonProjectUri; return this; }
        public Builder baseCommitId(String baseCommitId) { this.baseCommitId = baseCommitId; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public WorkspaceBinding build() {
            return new WorkspaceBinding(bindingId, tenantId, projectId, modelProjectId, modelProjectName,
                    primaryChannel, channelLockToken, channelLockExpiresAt, currentWorkspaceState, boundUserId,
                    sysonProjectUri, baseCommitId, createdAt, updatedAt);
        }
    }

    public Long getBindingId() { return bindingId; }
    public void setBindingId(Long bindingId) { this.bindingId = bindingId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getModelProjectId() { return modelProjectId; }
    public void setModelProjectId(String modelProjectId) { this.modelProjectId = modelProjectId; }

    public String getModelProjectName() { return modelProjectName; }
    public void setModelProjectName(String modelProjectName) { this.modelProjectName = modelProjectName; }

    public String getPrimaryChannel() { return primaryChannel; }
    public void setPrimaryChannel(String primaryChannel) { this.primaryChannel = primaryChannel; }

    public String getChannelLockToken() { return channelLockToken; }
    public void setChannelLockToken(String channelLockToken) { this.channelLockToken = channelLockToken; }

    public Instant getChannelLockExpiresAt() { return channelLockExpiresAt; }
    public void setChannelLockExpiresAt(Instant channelLockExpiresAt) { this.channelLockExpiresAt = channelLockExpiresAt; }

    public String getCurrentWorkspaceState() { return currentWorkspaceState; }
    public void setCurrentWorkspaceState(String currentWorkspaceState) { this.currentWorkspaceState = currentWorkspaceState; }

    public String getBoundUserId() { return boundUserId; }
    public void setBoundUserId(String boundUserId) { this.boundUserId = boundUserId; }

    public String getSysonProjectUri() { return sysonProjectUri; }
    public void setSysonProjectUri(String sysonProjectUri) { this.sysonProjectUri = sysonProjectUri; }

    public String getBaseCommitId() { return baseCommitId; }
    public void setBaseCommitId(String baseCommitId) { this.baseCommitId = baseCommitId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
