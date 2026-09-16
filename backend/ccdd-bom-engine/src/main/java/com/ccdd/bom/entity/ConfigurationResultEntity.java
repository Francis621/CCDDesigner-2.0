package com.ccdd.bom.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 确定性配置求解结果固化快照实体 (映射 sys_configuration_results 表)
 * 绝对不可变快照，持久化 100% BOM 结构与每行规则溯源，防止母版升版后产生漂移
 */
public class ConfigurationResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long resultId;
    private String tenantId;
    private String orderId;
    private Long structureRevisionId;
    private Long ruleSetRevId;
    private String inputSelectionsJson;
    private String resolved100BomJson;
    private String provenanceTraceJson;
    private String resultDigestSha256;
    private Integer solverDurationMs;
    private String evaluatedBy;
    private Instant createdAt;

    public ConfigurationResultEntity() {
    }

    public ConfigurationResultEntity(Long resultId, String tenantId, String orderId, Long structureRevisionId,
                                   Long ruleSetRevId, String inputSelectionsJson, String resolved100BomJson,
                                   String provenanceTraceJson, String resultDigestSha256, Integer solverDurationMs,
                                   String evaluatedBy, Instant createdAt) {
        this.resultId = resultId;
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.structureRevisionId = structureRevisionId;
        this.ruleSetRevId = ruleSetRevId;
        this.inputSelectionsJson = inputSelectionsJson;
        this.resolved100BomJson = resolved100BomJson;
        this.provenanceTraceJson = provenanceTraceJson;
        this.resultDigestSha256 = resultDigestSha256;
        this.solverDurationMs = solverDurationMs;
        this.evaluatedBy = evaluatedBy;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long resultId;
        private String tenantId;
        private String orderId;
        private Long structureRevisionId;
        private Long ruleSetRevId;
        private String inputSelectionsJson;
        private String resolved100BomJson;
        private String provenanceTraceJson;
        private String resultDigestSha256;
        private Integer solverDurationMs;
        private String evaluatedBy;
        private Instant createdAt;

        public Builder resultId(Long resultId) { this.resultId = resultId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder orderId(String orderId) { this.orderId = orderId; return this; }
        public Builder structureRevisionId(Long structureRevisionId) { this.structureRevisionId = structureRevisionId; return this; }
        public Builder ruleSetRevId(Long ruleSetRevId) { this.ruleSetRevId = ruleSetRevId; return this; }
        public Builder inputSelectionsJson(String inputSelectionsJson) { this.inputSelectionsJson = inputSelectionsJson; return this; }
        public Builder resolved100BomJson(String resolved100BomJson) { this.resolved100BomJson = resolved100BomJson; return this; }
        public Builder provenanceTraceJson(String provenanceTraceJson) { this.provenanceTraceJson = provenanceTraceJson; return this; }
        public Builder resultDigestSha256(String resultDigestSha256) { this.resultDigestSha256 = resultDigestSha256; return this; }
        public Builder solverDurationMs(Integer solverDurationMs) { this.solverDurationMs = solverDurationMs; return this; }
        public Builder evaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ConfigurationResultEntity build() {
            return new ConfigurationResultEntity(resultId, tenantId, orderId, structureRevisionId,
                    ruleSetRevId, inputSelectionsJson, resolved100BomJson, provenanceTraceJson,
                    resultDigestSha256, solverDurationMs, evaluatedBy, createdAt);
        }
    }

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Long getStructureRevisionId() { return structureRevisionId; }
    public void setStructureRevisionId(Long structureRevisionId) { this.structureRevisionId = structureRevisionId; }

    public Long getRuleSetRevId() { return ruleSetRevId; }
    public void setRuleSetRevId(Long ruleSetRevId) { this.ruleSetRevId = ruleSetRevId; }

    public String getInputSelectionsJson() { return inputSelectionsJson; }
    public void setInputSelectionsJson(String inputSelectionsJson) { this.inputSelectionsJson = inputSelectionsJson; }

    public String getResolved100BomJson() { return resolved100BomJson; }
    public void setResolved100BomJson(String resolved100BomJson) { this.resolved100BomJson = resolved100BomJson; }

    public String getProvenanceTraceJson() { return provenanceTraceJson; }
    public void setProvenanceTraceJson(String provenanceTraceJson) { this.provenanceTraceJson = provenanceTraceJson; }

    public String getResultDigestSha256() { return resultDigestSha256; }
    public void setResultDigestSha256(String resultDigestSha256) { this.resultDigestSha256 = resultDigestSha256; }

    public Integer getSolverDurationMs() { return solverDurationMs; }
    public void setSolverDurationMs(Integer solverDurationMs) { this.solverDurationMs = solverDurationMs; }

    public String getEvaluatedBy() { return evaluatedBy; }
    public void setEvaluatedBy(String evaluatedBy) { this.evaluatedBy = evaluatedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
