package com.ccdd.manufacturing.entity;

import java.time.Instant;

/**
 * 工艺路线实体 (BOP Process Plan)
 * 映射物理表 sys_process_plans
 */
public class ProcessPlanEntity {

    private Long planId;
    private String tenantId;
    private String routingCode;
    private String routingName;
    private Long mbomRevisionId;
    private String plantCode;
    private String lifecycleState;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;

    public ProcessPlanEntity() {
    }

    public ProcessPlanEntity(Long planId, String tenantId, String routingCode, String routingName,
                             Long mbomRevisionId, String plantCode, String lifecycleState,
                             String createdBy, Instant createdAt, Instant updatedAt) {
        this.planId = planId;
        this.tenantId = tenantId;
        this.routingCode = routingCode;
        this.routingName = routingName;
        this.mbomRevisionId = mbomRevisionId;
        this.plantCode = plantCode;
        this.lifecycleState = lifecycleState;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long planId;
        private String tenantId;
        private String routingCode;
        private String routingName;
        private Long mbomRevisionId;
        private String plantCode = "PLANT_01";
        private String lifecycleState = "DRAFT";
        private String createdBy = "PROCESS_ENGINEER";
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder planId(Long planId) {
            this.planId = planId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder routingCode(String routingCode) {
            this.routingCode = routingCode;
            return this;
        }

        public Builder routingName(String routingName) {
            this.routingName = routingName;
            return this;
        }

        public Builder mbomRevisionId(Long mbomRevisionId) {
            this.mbomRevisionId = mbomRevisionId;
            return this;
        }

        public Builder plantCode(String plantCode) {
            this.plantCode = plantCode;
            return this;
        }

        public Builder lifecycleState(String lifecycleState) {
            this.lifecycleState = lifecycleState;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ProcessPlanEntity build() {
            return new ProcessPlanEntity(planId, tenantId, routingCode, routingName,
                    mbomRevisionId, plantCode, lifecycleState, createdBy, createdAt, updatedAt);
        }
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getRoutingCode() {
        return routingCode;
    }

    public void setRoutingCode(String routingCode) {
        this.routingCode = routingCode;
    }

    public String getRoutingName() {
        return routingName;
    }

    public void setRoutingName(String routingName) {
        this.routingName = routingName;
    }

    public Long getMbomRevisionId() {
        return mbomRevisionId;
    }

    public void setMbomRevisionId(Long mbomRevisionId) {
        this.mbomRevisionId = mbomRevisionId;
    }

    public String getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(String plantCode) {
        this.plantCode = plantCode;
    }

    public String getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
