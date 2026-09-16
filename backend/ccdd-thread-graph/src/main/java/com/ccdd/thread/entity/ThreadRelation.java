package com.ccdd.thread.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 数字主线拓扑关联关系边实体 (映射 ccdd_thread_relations 表)
 * 表达跨领域上下游关联 (如 REFINES, SATISFIES, DERIVED_FROM, ALLOCATED_TO, VERIFIES)
 */
public class ThreadRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long relationId;
    private String tenantId;
    private String sourceNodeId;
    private String targetNodeId;
    private String relationType;
    private Boolean isBidirectional;
    private Boolean isBaselineLocked;
    private String baselineId;
    private String attributesJson;
    private Instant createdAt;

    public ThreadRelation() {
    }

    public ThreadRelation(Long relationId, String tenantId, String sourceNodeId, String targetNodeId,
                          String relationType, Boolean isBidirectional, Boolean isBaselineLocked,
                          String baselineId, String attributesJson, Instant createdAt) {
        this.relationId = relationId;
        this.tenantId = tenantId;
        this.sourceNodeId = sourceNodeId;
        this.targetNodeId = targetNodeId;
        this.relationType = relationType;
        this.isBidirectional = isBidirectional;
        this.isBaselineLocked = isBaselineLocked;
        this.baselineId = baselineId;
        this.attributesJson = attributesJson;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long relationId;
        private String tenantId;
        private String sourceNodeId;
        private String targetNodeId;
        private String relationType;
        private Boolean isBidirectional;
        private Boolean isBaselineLocked;
        private String baselineId;
        private String attributesJson;
        private Instant createdAt;

        public Builder relationId(Long relationId) { this.relationId = relationId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder sourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; return this; }
        public Builder targetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; return this; }
        public Builder relationType(String relationType) { this.relationType = relationType; return this; }
        public Builder isBidirectional(Boolean isBidirectional) { this.isBidirectional = isBidirectional; return this; }
        public Builder isBaselineLocked(Boolean isBaselineLocked) { this.isBaselineLocked = isBaselineLocked; return this; }
        public Builder baselineId(String baselineId) { this.baselineId = baselineId; return this; }
        public Builder attributesJson(String attributesJson) { this.attributesJson = attributesJson; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ThreadRelation build() {
            return new ThreadRelation(relationId, tenantId, sourceNodeId, targetNodeId, relationType,
                    isBidirectional, isBaselineLocked, baselineId, attributesJson, createdAt);
        }
    }

    public Long getRelationId() { return relationId; }
    public void setRelationId(Long relationId) { this.relationId = relationId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getSourceNodeId() { return sourceNodeId; }
    public void setSourceNodeId(String sourceNodeId) { this.sourceNodeId = sourceNodeId; }

    public String getTargetNodeId() { return targetNodeId; }
    public void setTargetNodeId(String targetNodeId) { this.targetNodeId = targetNodeId; }

    public String getRelationType() { return relationType; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    public Boolean getIsBidirectional() { return isBidirectional; }
    public void setIsBidirectional(Boolean isBidirectional) { this.isBidirectional = isBidirectional; }

    public Boolean getIsBaselineLocked() { return isBaselineLocked; }
    public void setIsBaselineLocked(Boolean isBaselineLocked) { this.isBaselineLocked = isBaselineLocked; }

    public String getBaselineId() { return baselineId; }
    public void setBaselineId(String baselineId) { this.baselineId = baselineId; }

    public String getAttributesJson() { return attributesJson; }
    public void setAttributesJson(String attributesJson) { this.attributesJson = attributesJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
