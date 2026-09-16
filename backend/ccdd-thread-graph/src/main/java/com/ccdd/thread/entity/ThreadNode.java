package com.ccdd.thread.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 数字主线统一图节点实体 (映射 ccdd_thread_nodes 表)
 * 具备全局跨领域统一编址能力，涵盖 REQUIREMENT, ARCHITECTURE, CAD, CAE, BOM 等核心领域
 */
public class ThreadNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String tenantId;
    private String domainType;
    private String entityId;
    private String displayName;
    private String version;
    private String lifecycleState;
    private String attributesJson;
    private Instant createdAt;

    public ThreadNode() {
    }

    public ThreadNode(String nodeId, String tenantId, String domainType, String entityId,
                      String displayName, String version, String lifecycleState,
                      String attributesJson, Instant createdAt) {
        this.nodeId = nodeId;
        this.tenantId = tenantId;
        this.domainType = domainType;
        this.entityId = entityId;
        this.displayName = displayName;
        this.version = version;
        this.lifecycleState = lifecycleState;
        this.attributesJson = attributesJson;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String nodeId;
        private String tenantId;
        private String domainType;
        private String entityId;
        private String displayName;
        private String version;
        private String lifecycleState;
        private String attributesJson;
        private Instant createdAt;

        public Builder nodeId(String nodeId) { this.nodeId = nodeId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder domainType(String domainType) { this.domainType = domainType; return this; }
        public Builder entityId(String entityId) { this.entityId = entityId; return this; }
        public Builder displayName(String displayName) { this.displayName = displayName; return this; }
        public Builder version(String version) { this.version = version; return this; }
        public Builder lifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; return this; }
        public Builder attributesJson(String attributesJson) { this.attributesJson = attributesJson; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ThreadNode build() {
            return new ThreadNode(nodeId, tenantId, domainType, entityId, displayName, version,
                    lifecycleState, attributesJson, createdAt);
        }
    }

    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getDomainType() { return domainType; }
    public void setDomainType(String domainType) { this.domainType = domainType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getLifecycleState() { return lifecycleState; }
    public void setLifecycleState(String lifecycleState) { this.lifecycleState = lifecycleState; }

    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
