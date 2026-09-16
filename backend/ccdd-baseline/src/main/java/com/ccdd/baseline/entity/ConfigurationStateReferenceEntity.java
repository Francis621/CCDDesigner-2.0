package com.ccdd.baseline.entity;

import java.time.Instant;

/**
 * M21 多形态配置状态引用实体 (ConfigurationStateReference)
 * 承接 As-Designed 到 As-Maintained 多形态配置全生命周期解耦
 */
public class ConfigurationStateReferenceEntity {

    private Long configRefId;
    private String tenantId;
    private BaselinePurpose configStateType;
    private Long baselineId;
    private Long orderProductId;
    private Long individualId;
    private String serialNumber; // 单机序列号，例如 VMC850-202603-001
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private Boolean isActive;
    private String notes;
    private Instant boundAt;
    private String boundBy;

    public ConfigurationStateReferenceEntity() {
    }

    public ConfigurationStateReferenceEntity(Long configRefId, String tenantId, BaselinePurpose configStateType,
                                             Long baselineId, Long orderProductId, Long individualId,
                                             String serialNumber, Instant effectiveFrom, Instant effectiveTo,
                                             Boolean isActive, String notes, Instant boundAt, String boundBy) {
        this.configRefId = configRefId;
        this.tenantId = tenantId;
        this.configStateType = configStateType;
        this.baselineId = baselineId;
        this.orderProductId = orderProductId;
        this.individualId = individualId;
        this.serialNumber = serialNumber;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.isActive = isActive;
        this.notes = notes;
        this.boundAt = boundAt;
        this.boundBy = boundBy;
    }

    public Long getConfigRefId() {
        return configRefId;
    }

    public void setConfigRefId(Long configRefId) {
        this.configRefId = configRefId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public BaselinePurpose getConfigStateType() {
        return configStateType;
    }

    public void setConfigStateType(BaselinePurpose configStateType) {
        this.configStateType = configStateType;
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public Long getOrderProductId() {
        return orderProductId;
    }

    public void setOrderProductId(Long orderProductId) {
        this.orderProductId = orderProductId;
    }

    public Long getIndividualId() {
        return individualId;
    }

    public void setIndividualId(Long individualId) {
        this.individualId = individualId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public Instant getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Instant effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public Instant getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(Instant effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getBoundAt() {
        return boundAt;
    }

    public void setBoundAt(Instant boundAt) {
        this.boundAt = boundAt;
    }

    public String getBoundBy() {
        return boundBy;
    }

    public void setBoundBy(String boundBy) {
        this.boundBy = boundBy;
    }
}
