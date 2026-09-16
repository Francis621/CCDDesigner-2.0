package com.ccdd.baseline.dto;

import com.ccdd.baseline.entity.BaselinePurpose;

/**
 * 绑定多形态配置状态请求 DTO
 */
public class BindConfigurationStateRequest {

    private Long baselineId;
    private BaselinePurpose configStateType; // AS_DESIGNED, AS_PLANNED, AS_BUILT, AS_DELIVERED, AS_MAINTAINED
    private Long orderProductId;
    private Long individualId;
    private String serialNumber; // 单机序列号，如 VMC850-202603-001
    private String notes;
    private String boundBy;

    public BindConfigurationStateRequest() {
    }

    public Long getBaselineId() {
        return baselineId;
    }

    public void setBaselineId(Long baselineId) {
        this.baselineId = baselineId;
    }

    public BaselinePurpose getConfigStateType() {
        return configStateType;
    }

    public void setConfigStateType(BaselinePurpose configStateType) {
        this.configStateType = configStateType;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getBoundBy() {
        return boundBy;
    }

    public void setBoundBy(String boundBy) {
        this.boundBy = boundBy;
    }
}
