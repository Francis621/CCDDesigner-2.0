package com.ccdd.workflow.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * M24: 业务类型与 BPMN 流程定义映射配置实体
 */
public class DefinitionBindingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bindingId;
    private String targetObjectType;
    private String businessCategory;
    private String flowableProcDefKey;
    private Integer procDefVersion;
    private SignStrategy signStrategy;
    private BigDecimal passThresholdPercent;
    private Boolean isActive;
    private String bindingDescription;
    private Instant createdAt;

    public DefinitionBindingEntity() {
    }

    public DefinitionBindingEntity(Long bindingId, String targetObjectType, String businessCategory,
                                   String flowableProcDefKey, Integer procDefVersion, SignStrategy signStrategy,
                                   BigDecimal passThresholdPercent, Boolean isActive, String bindingDescription,
                                   Instant createdAt) {
        this.bindingId = bindingId;
        this.targetObjectType = targetObjectType;
        this.businessCategory = businessCategory;
        this.flowableProcDefKey = flowableProcDefKey;
        this.procDefVersion = procDefVersion;
        this.signStrategy = signStrategy;
        this.passThresholdPercent = passThresholdPercent;
        this.isActive = isActive;
        this.bindingDescription = bindingDescription;
        this.createdAt = createdAt;
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

    public String getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(String businessCategory) {
        this.businessCategory = businessCategory;
    }

    public String getFlowableProcDefKey() {
        return flowableProcDefKey;
    }

    public void setFlowableProcDefKey(String flowableProcDefKey) {
        this.flowableProcDefKey = flowableProcDefKey;
    }

    public Integer getProcDefVersion() {
        return procDefVersion;
    }

    public void setProcDefVersion(Integer procDefVersion) {
        this.procDefVersion = procDefVersion;
    }

    public SignStrategy getSignStrategy() {
        return signStrategy;
    }

    public void setSignStrategy(SignStrategy signStrategy) {
        this.signStrategy = signStrategy;
    }

    public BigDecimal getPassThresholdPercent() {
        return passThresholdPercent;
    }

    public void setPassThresholdPercent(BigDecimal passThresholdPercent) {
        this.passThresholdPercent = passThresholdPercent;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getBindingDescription() {
        return bindingDescription;
    }

    public void setBindingDescription(String bindingDescription) {
        this.bindingDescription = bindingDescription;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
