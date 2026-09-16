package com.ccdd.manufacturing.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * BOP 工艺路线与工序物料明细详情 DTO
 */
public class ProcessPlanDetailDto {

    private Long planId;
    private String routingCode;
    private String routingName;
    private Long mbomRevisionId;
    private String plantCode;
    private String lifecycleState;
    private List<OperationDetailDto> operations = new ArrayList<>();

    public ProcessPlanDetailDto() {
    }

    public ProcessPlanDetailDto(Long planId, String routingCode, String routingName, Long mbomRevisionId,
                                String plantCode, String lifecycleState, List<OperationDetailDto> operations) {
        this.planId = planId;
        this.routingCode = routingCode;
        this.routingName = routingName;
        this.mbomRevisionId = mbomRevisionId;
        this.plantCode = plantCode;
        this.lifecycleState = lifecycleState;
        this.operations = operations;
    }

    public static class OperationDetailDto {
        private Long operationId;
        private Integer sequenceNumber;
        private String operationCode;
        private String operationName;
        private String workCenterCode;
        private BigDecimal setupTimeMins;
        private BigDecimal runTimeMins;
        private String toolingFixtures;
        private String inspectionRequirement;
        private List<AllocatedPartDto> allocatedParts = new ArrayList<>();

        public OperationDetailDto() {
        }

        public OperationDetailDto(Long operationId, Integer sequenceNumber, String operationCode,
                                  String operationName, String workCenterCode, BigDecimal setupTimeMins,
                                  BigDecimal runTimeMins, String toolingFixtures, String inspectionRequirement,
                                  List<AllocatedPartDto> allocatedParts) {
            this.operationId = operationId;
            this.sequenceNumber = sequenceNumber;
            this.operationCode = operationCode;
            this.operationName = operationName;
            this.workCenterCode = workCenterCode;
            this.setupTimeMins = setupTimeMins;
            this.runTimeMins = runTimeMins;
            this.toolingFixtures = toolingFixtures;
            this.inspectionRequirement = inspectionRequirement;
            this.allocatedParts = allocatedParts;
        }

        public Long getOperationId() {
            return operationId;
        }

        public void setOperationId(Long operationId) {
            this.operationId = operationId;
        }

        public Integer getSequenceNumber() {
            return sequenceNumber;
        }

        public void setSequenceNumber(Integer sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        public String getOperationCode() {
            return operationCode;
        }

        public void setOperationCode(String operationCode) {
            this.operationCode = operationCode;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public String getWorkCenterCode() {
            return workCenterCode;
        }

        public void setWorkCenterCode(String workCenterCode) {
            this.workCenterCode = workCenterCode;
        }

        public BigDecimal getSetupTimeMins() {
            return setupTimeMins;
        }

        public void setSetupTimeMins(BigDecimal setupTimeMins) {
            this.setupTimeMins = setupTimeMins;
        }

        public BigDecimal getRunTimeMins() {
            return runTimeMins;
        }

        public void setRunTimeMins(BigDecimal runTimeMins) {
            this.runTimeMins = runTimeMins;
        }

        public String getToolingFixtures() {
            return toolingFixtures;
        }

        public void setToolingFixtures(String toolingFixtures) {
            this.toolingFixtures = toolingFixtures;
        }

        public String getInspectionRequirement() {
            return inspectionRequirement;
        }

        public void setInspectionRequirement(String inspectionRequirement) {
            this.inspectionRequirement = inspectionRequirement;
        }

        public List<AllocatedPartDto> getAllocatedParts() {
            return allocatedParts;
        }

        public void setAllocatedParts(List<AllocatedPartDto> allocatedParts) {
            this.allocatedParts = allocatedParts;
        }
    }

    public static class AllocatedPartDto {
        private String partNumber;
        private String partName;
        private BigDecimal consumedQuantity;
        private String unitOfMeasure;
        private String transformType;

        public AllocatedPartDto() {
        }

        public AllocatedPartDto(String partNumber, String partName, BigDecimal consumedQuantity,
                                String unitOfMeasure, String transformType) {
            this.partNumber = partNumber;
            this.partName = partName;
            this.consumedQuantity = consumedQuantity;
            this.unitOfMeasure = unitOfMeasure;
            this.transformType = transformType;
        }

        public String getPartNumber() {
            return partNumber;
        }

        public void setPartNumber(String partNumber) {
            this.partNumber = partNumber;
        }

        public String getPartName() {
            return partName;
        }

        public void setPartName(String partName) {
            this.partName = partName;
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

        public String getTransformType() {
            return transformType;
        }

        public void setTransformType(String transformType) {
            this.transformType = transformType;
        }
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
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

    public List<OperationDetailDto> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationDetailDto> operations) {
        this.operations = operations;
    }
}
