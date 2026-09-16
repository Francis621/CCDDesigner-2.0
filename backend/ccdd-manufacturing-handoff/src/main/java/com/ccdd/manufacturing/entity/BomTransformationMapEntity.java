package com.ccdd.manufacturing.entity;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * EBOM/MBOM 转换映射与消耗平衡矩阵实体
 * 映射物理表 sys_bom_transformation_maps
 */
public class BomTransformationMapEntity {

    private Long mapId;
    private String tenantId;
    private Long mbomRevisionId;
    private Long ebomLineId;
    private String sourcePartNumber;
    private String mbomLineNumber;
    private String targetPartNumber;
    private TransformationType transformType;
    private BigDecimal consumedQuantity;
    private String unitOfMeasure;
    private Integer operationSequence;
    private Instant createdAt;

    public BomTransformationMapEntity() {
    }

    public BomTransformationMapEntity(Long mapId, String tenantId, Long mbomRevisionId, Long ebomLineId,
                                      String sourcePartNumber, String mbomLineNumber, String targetPartNumber,
                                      TransformationType transformType, BigDecimal consumedQuantity,
                                      String unitOfMeasure, Integer operationSequence, Instant createdAt) {
        this.mapId = mapId;
        this.tenantId = tenantId;
        this.mbomRevisionId = mbomRevisionId;
        this.ebomLineId = ebomLineId;
        this.sourcePartNumber = sourcePartNumber;
        this.mbomLineNumber = mbomLineNumber;
        this.targetPartNumber = targetPartNumber;
        this.transformType = transformType;
        this.consumedQuantity = consumedQuantity;
        this.unitOfMeasure = unitOfMeasure;
        this.operationSequence = operationSequence;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long mapId;
        private String tenantId;
        private Long mbomRevisionId;
        private Long ebomLineId;
        private String sourcePartNumber;
        private String mbomLineNumber;
        private String targetPartNumber;
        private TransformationType transformType = TransformationType.DIRECT_1_TO_1;
        private BigDecimal consumedQuantity = BigDecimal.ONE;
        private String unitOfMeasure = "EA";
        private Integer operationSequence;
        private Instant createdAt = Instant.now();

        public Builder mapId(Long mapId) {
            this.mapId = mapId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder mbomRevisionId(Long mbomRevisionId) {
            this.mbomRevisionId = mbomRevisionId;
            return this;
        }

        public Builder ebomLineId(Long ebomLineId) {
            this.ebomLineId = ebomLineId;
            return this;
        }

        public Builder sourcePartNumber(String sourcePartNumber) {
            this.sourcePartNumber = sourcePartNumber;
            return this;
        }

        public Builder mbomLineNumber(String mbomLineNumber) {
            this.mbomLineNumber = mbomLineNumber;
            return this;
        }

        public Builder targetPartNumber(String targetPartNumber) {
            this.targetPartNumber = targetPartNumber;
            return this;
        }

        public Builder transformType(TransformationType transformType) {
            this.transformType = transformType;
            return this;
        }

        public Builder consumedQuantity(BigDecimal consumedQuantity) {
            this.consumedQuantity = consumedQuantity;
            return this;
        }

        public Builder unitOfMeasure(String unitOfMeasure) {
            this.unitOfMeasure = unitOfMeasure;
            return this;
        }

        public Builder operationSequence(Integer operationSequence) {
            this.operationSequence = operationSequence;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BomTransformationMapEntity build() {
            return new BomTransformationMapEntity(mapId, tenantId, mbomRevisionId, ebomLineId,
                    sourcePartNumber, mbomLineNumber, targetPartNumber, transformType,
                    consumedQuantity, unitOfMeasure, operationSequence, createdAt);
        }
    }

    public Long getMapId() {
        return mapId;
    }

    public void setMapId(Long mapId) {
        this.mapId = mapId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getMbomRevisionId() {
        return mbomRevisionId;
    }

    public void setMbomRevisionId(Long mbomRevisionId) {
        this.mbomRevisionId = mbomRevisionId;
    }

    public Long getEbomLineId() {
        return ebomLineId;
    }

    public void setEbomLineId(Long ebomLineId) {
        this.ebomLineId = ebomLineId;
    }

    public String getSourcePartNumber() {
        return sourcePartNumber;
    }

    public void setSourcePartNumber(String sourcePartNumber) {
        this.sourcePartNumber = sourcePartNumber;
    }

    public String getMbomLineNumber() {
        return mbomLineNumber;
    }

    public void setMbomLineNumber(String mbomLineNumber) {
        this.mbomLineNumber = mbomLineNumber;
    }

    public String getTargetPartNumber() {
        return targetPartNumber;
    }

    public void setTargetPartNumber(String targetPartNumber) {
        this.targetPartNumber = targetPartNumber;
    }

    public TransformationType getTransformType() {
        return transformType;
    }

    public void setTransformType(TransformationType transformType) {
        this.transformType = transformType;
    }

    public BigDecimal getConsumedQuantity() {
        return consumedQuantity;
    }

    public void setConsumedQuantity(BigDecimal consumedQuantity) {
        this.consumedQuantity = consumedQuantity;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public Integer getOperationSequence() {
        return operationSequence;
    }

    public void setOperationSequence(Integer operationSequence) {
        this.operationSequence = operationSequence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
