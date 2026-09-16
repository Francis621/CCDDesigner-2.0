package com.ccdd.change.dto;

import java.util.List;

/**
 * 触发影响面拓扑分析请求 DTO (OpenAPI §8.1)
 */
public class EvaluateImpactRequest {

    private Long seedRevisionId;
    private Integer maxDepth; // 默认 5
    private List<String> relationTypes; // allocatedTo, satisfies, verifies, usesRevision 等
    private Long contextConfigId;

    public EvaluateImpactRequest() {
        this.maxDepth = 5;
    }

    public EvaluateImpactRequest(Long seedRevisionId, Integer maxDepth, List<String> relationTypes, Long contextConfigId) {
        this.seedRevisionId = seedRevisionId;
        this.maxDepth = maxDepth != null ? maxDepth : 5;
        this.relationTypes = relationTypes;
        this.contextConfigId = contextConfigId;
    }

    public Long getSeedRevisionId() {
        return seedRevisionId;
    }

    public void setSeedRevisionId(Long seedRevisionId) {
        this.seedRevisionId = seedRevisionId;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public List<String> getRelationTypes() {
        return relationTypes;
    }

    public void setRelationTypes(List<String> relationTypes) {
        this.relationTypes = relationTypes;
    }

    public Long getContextConfigId() {
        return contextConfigId;
    }

    public void setContextConfigId(Long contextConfigId) {
        this.contextConfigId = contextConfigId;
    }
}
