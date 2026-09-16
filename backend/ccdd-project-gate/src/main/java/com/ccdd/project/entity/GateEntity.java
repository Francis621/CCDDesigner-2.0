package com.ccdd.project.entity;

import java.time.Instant;

/**
 * 阶段出口评审门卡实体 (Stage Gate)
 * 映射 plm_project.gate
 * 铁律：Program 严禁设立 Gate，仅 Project 内部 Stage 可挂接 Gate
 */
public class GateEntity {

    private Long gateId;
    private Long stageId;
    private String gateCode;
    private String name;
    private String description;
    private String reviewWorkflowDef;
    private String status; // INIT, READY, IN_REVIEW, DECIDED
    private Instant createdAt;

    public GateEntity() {
    }

    public GateEntity(Long gateId, Long stageId, String gateCode, String name, String description,
                      String reviewWorkflowDef, String status, Instant createdAt) {
        this.gateId = gateId;
        this.stageId = stageId;
        this.gateCode = gateCode;
        this.name = name;
        this.description = description;
        this.reviewWorkflowDef = reviewWorkflowDef;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long gateId;
        private Long stageId;
        private String gateCode;
        private String name;
        private String description;
        private String reviewWorkflowDef = "gate_review_standard_process";
        private String status = "INIT";
        private Instant createdAt = Instant.now();

        public Builder gateId(Long gateId) { this.gateId = gateId; return this; }
        public Builder stageId(Long stageId) { this.stageId = stageId; return this; }
        public Builder gateCode(String gateCode) { this.gateCode = gateCode; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder reviewWorkflowDef(String reviewWorkflowDef) { this.reviewWorkflowDef = reviewWorkflowDef; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public GateEntity build() {
            return new GateEntity(gateId, stageId, gateCode, name, description, reviewWorkflowDef, status, createdAt);
        }
    }

    public Long getGateId() { return gateId; }
    public void setGateId(Long gateId) { this.gateId = gateId; }
    public Long getStageId() { return stageId; }
    public void setStageId(Long stageId) { this.stageId = stageId; }
    public String getGateCode() { return gateCode; }
    public void setGateCode(String gateCode) { this.gateCode = gateCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReviewWorkflowDef() { return reviewWorkflowDef; }
    public void setReviewWorkflowDef(String reviewWorkflowDef) { this.reviewWorkflowDef = reviewWorkflowDef; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
