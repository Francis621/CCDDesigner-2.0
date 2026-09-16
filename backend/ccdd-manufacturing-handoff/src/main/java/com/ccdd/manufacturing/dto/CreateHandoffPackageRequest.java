package com.ccdd.manufacturing.dto;

/**
 * 创建制造下发批次包请求 DTO
 */
public class CreateHandoffPackageRequest {

    private Long mbomRevisionId;
    private String targetSystem; // 如 MES_PLANT_01, ERP_SAP
    private String operatorName;

    public CreateHandoffPackageRequest() {
    }

    public CreateHandoffPackageRequest(Long mbomRevisionId, String targetSystem, String operatorName) {
        this.mbomRevisionId = mbomRevisionId;
        this.targetSystem = targetSystem;
        this.operatorName = operatorName;
    }

    public Long getMbomRevisionId() {
        return mbomRevisionId;
    }

    public void setMbomRevisionId(Long mbomRevisionId) {
        this.mbomRevisionId = mbomRevisionId;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
