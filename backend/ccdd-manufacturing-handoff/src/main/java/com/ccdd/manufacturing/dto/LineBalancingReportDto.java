package com.ccdd.manufacturing.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 装配线平衡率与瓶颈优化分析报告 DTO
 * 落实工业工程 (IE) 生产线平衡度与节拍平滑模型
 */
public class LineBalancingReportDto {

    private Long planId;
    private String routingCode;
    private String routingName;
    private Integer totalStations;
    private BigDecimal totalWorkContentMins;
    private BigDecimal cycleTimeMins; // 生产线节拍 (最大工位工时)
    private BigDecimal lineBalancingEfficiency; // 平衡率 (LBE, %)
    private BigDecimal balanceDelayPercentage;  // 平衡损失率 (BD, %)
    private BigDecimal smoothnessIndex;        // 平滑指数 (SI)
    private String bottleneckOperationCode;    // 瓶颈工序号 (如 OP40)
    private List<StationWorkloadDto> stationWorkloads = new ArrayList<>();
    private List<BalancingOptimizationProposalDto> optimizationProposals = new ArrayList<>();

    public LineBalancingReportDto() {
    }

    public static class StationWorkloadDto {
        private Integer sequenceNumber;
        private String operationCode;
        private String operationName;
        private String workCenterCode;
        private BigDecimal setupTimeMins;
        private BigDecimal runTimeMins;
        private BigDecimal totalTimeMins;
        private BigDecimal taktPercentage; // 占节拍百分比
        private Boolean isBottleneck;

        public StationWorkloadDto() {
        }

        public StationWorkloadDto(Integer sequenceNumber, String operationCode, String operationName,
                                  String workCenterCode, BigDecimal setupTimeMins, BigDecimal runTimeMins,
                                  BigDecimal totalTimeMins, BigDecimal taktPercentage, Boolean isBottleneck) {
            this.sequenceNumber = sequenceNumber;
            this.operationCode = operationCode;
            this.operationName = operationName;
            this.workCenterCode = workCenterCode;
            this.setupTimeMins = setupTimeMins;
            this.runTimeMins = runTimeMins;
            this.totalTimeMins = totalTimeMins;
            this.taktPercentage = taktPercentage;
            this.isBottleneck = isBottleneck;
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

        public BigDecimal getTotalTimeMins() {
            return totalTimeMins;
        }

        public void setTotalTimeMins(BigDecimal totalTimeMins) {
            this.totalTimeMins = totalTimeMins;
        }

        public BigDecimal getTaktPercentage() {
            return taktPercentage;
        }

        public void setTaktPercentage(BigDecimal taktPercentage) {
            this.taktPercentage = taktPercentage;
        }

        public Boolean getIsBottleneck() {
            return isBottleneck;
        }

        public void setIsBottleneck(Boolean bottleneck) {
            isBottleneck = bottleneck;
        }
    }

    public static class BalancingOptimizationProposalDto {
        private String proposalCode;
        private String proposalName;
        private String strategyType; // PARALLEL_WORKSTATION, OPERATION_DECOMPOSITION
        private String description;
        private BigDecimal projectedCycleTimeMins;
        private BigDecimal projectedEfficiency; // 优化后预期平衡率 (%)
        private BigDecimal efficiencyGain;      // 平衡率提升净值 (%)

        public BalancingOptimizationProposalDto() {
        }

        public BalancingOptimizationProposalDto(String proposalCode, String proposalName, String strategyType,
                                                String description, BigDecimal projectedCycleTimeMins,
                                                BigDecimal projectedEfficiency, BigDecimal efficiencyGain) {
            this.proposalCode = proposalCode;
            this.proposalName = proposalName;
            this.strategyType = strategyType;
            this.description = description;
            this.projectedCycleTimeMins = projectedCycleTimeMins;
            this.projectedEfficiency = projectedEfficiency;
            this.efficiencyGain = efficiencyGain;
        }

        public String getProposalCode() {
            return proposalCode;
        }

        public void setProposalCode(String proposalCode) {
            this.proposalCode = proposalCode;
        }

        public String getProposalName() {
            return proposalName;
        }

        public void setProposalName(String proposalName) {
            this.proposalName = proposalName;
        }

        public String getStrategyType() {
            return strategyType;
        }

        public void setStrategyType(String strategyType) {
            this.strategyType = strategyType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getProjectedCycleTimeMins() {
            return projectedCycleTimeMins;
        }

        public void setProjectedCycleTimeMins(BigDecimal projectedCycleTimeMins) {
            this.projectedCycleTimeMins = projectedCycleTimeMins;
        }

        public BigDecimal getProjectedEfficiency() {
            return projectedEfficiency;
        }

        public void setProjectedEfficiency(BigDecimal projectedEfficiency) {
            this.projectedEfficiency = projectedEfficiency;
        }

        public BigDecimal getEfficiencyGain() {
            return efficiencyGain;
        }

        public void setEfficiencyGain(BigDecimal efficiencyGain) {
            this.efficiencyGain = efficiencyGain;
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

    public Integer getTotalStations() {
        return totalStations;
    }

    public void setTotalStations(Integer totalStations) {
        this.totalStations = totalStations;
    }

    public BigDecimal getTotalWorkContentMins() {
        return totalWorkContentMins;
    }

    public void setTotalWorkContentMins(BigDecimal totalWorkContentMins) {
        this.totalWorkContentMins = totalWorkContentMins;
    }

    public BigDecimal getCycleTimeMins() {
        return cycleTimeMins;
    }

    public void setCycleTimeMins(BigDecimal cycleTimeMins) {
        this.cycleTimeMins = cycleTimeMins;
    }

    public BigDecimal getLineBalancingEfficiency() {
        return lineBalancingEfficiency;
    }

    public void setLineBalancingEfficiency(BigDecimal lineBalancingEfficiency) {
        this.lineBalancingEfficiency = lineBalancingEfficiency;
    }

    public BigDecimal getBalanceDelayPercentage() {
        return balanceDelayPercentage;
    }

    public void setBalanceDelayPercentage(BigDecimal balanceDelayPercentage) {
        this.balanceDelayPercentage = balanceDelayPercentage;
    }

    public BigDecimal getSmoothnessIndex() {
        return smoothnessIndex;
    }

    public void setSmoothnessIndex(BigDecimal smoothnessIndex) {
        this.smoothnessIndex = smoothnessIndex;
    }

    public String getBottleneckOperationCode() {
        return bottleneckOperationCode;
    }

    public void setBottleneckOperationCode(String bottleneckOperationCode) {
        this.bottleneckOperationCode = bottleneckOperationCode;
    }

    public List<StationWorkloadDto> getStationWorkloads() {
        return stationWorkloads;
    }

    public void setStationWorkloads(List<StationWorkloadDto> stationWorkloads) {
        this.stationWorkloads = stationWorkloads;
    }

    public List<BalancingOptimizationProposalDto> getOptimizationProposals() {
        return optimizationProposals;
    }

    public void setOptimizationProposals(List<BalancingOptimizationProposalDto> optimizationProposals) {
        this.optimizationProposals = optimizationProposals;
    }
}
