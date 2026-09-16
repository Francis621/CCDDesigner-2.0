package com.ccdd.manufacturing.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * EBOM/MBOM 消耗平衡残差诊断报告 DTO
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 3.2 节数学模型
 */
public class ConsumptionBalanceReport {

    private Long mbomRevisionId;
    private Boolean isBalanced;
    private Integer totalEbomItemsCount;
    private Integer balancedItemsCount;
    private Integer underConsumedCount;
    private Integer overConsumedCount;
    private Integer illegalSourceCount;
    private List<ItemBalanceDetail> itemDetails = new ArrayList<>();
    private String summaryMessage;

    public ConsumptionBalanceReport() {
    }

    public ConsumptionBalanceReport(Long mbomRevisionId, Boolean isBalanced, Integer totalEbomItemsCount,
                                   Integer balancedItemsCount, Integer underConsumedCount,
                                   Integer overConsumedCount, Integer illegalSourceCount,
                                   List<ItemBalanceDetail> itemDetails, String summaryMessage) {
        this.mbomRevisionId = mbomRevisionId;
        this.isBalanced = isBalanced;
        this.totalEbomItemsCount = totalEbomItemsCount;
        this.balancedItemsCount = balancedItemsCount;
        this.underConsumedCount = underConsumedCount;
        this.overConsumedCount = overConsumedCount;
        this.illegalSourceCount = illegalSourceCount;
        this.itemDetails = itemDetails;
        this.summaryMessage = summaryMessage;
    }

    public static class ItemBalanceDetail {
        private String partNumber;
        private String partName;
        private BigDecimal ebomRequiredQty;
        private BigDecimal mbomConsumedQty;
        private BigDecimal residualDelta; // ΔQ = E_i - Σ M_{i,j}
        private String balanceStatus; // OK, UNDER_CONSUMED, OVER_CONSUMED, FABRICATED_SOURCE
        private String diagnosticMessage;

        public ItemBalanceDetail() {
        }

        public ItemBalanceDetail(String partNumber, String partName, BigDecimal ebomRequiredQty,
                                 BigDecimal mbomConsumedQty, BigDecimal residualDelta,
                                 String balanceStatus, String diagnosticMessage) {
            this.partNumber = partNumber;
            this.partName = partName;
            this.ebomRequiredQty = ebomRequiredQty;
            this.mbomConsumedQty = mbomConsumedQty;
            this.residualDelta = residualDelta;
            this.balanceStatus = balanceStatus;
            this.diagnosticMessage = diagnosticMessage;
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

        public BigDecimal getEbomRequiredQty() {
            return ebomRequiredQty;
        }

        public void setEbomRequiredQty(BigDecimal ebomRequiredQty) {
            this.ebomRequiredQty = ebomRequiredQty;
        }

        public BigDecimal getMbomConsumedQty() {
            return mbomConsumedQty;
        }

        public void setMbomConsumedQty(BigDecimal mbomConsumedQty) {
            this.mbomConsumedQty = mbomConsumedQty;
        }

        public BigDecimal getResidualDelta() {
            return residualDelta;
        }

        public void setResidualDelta(BigDecimal residualDelta) {
            this.residualDelta = residualDelta;
        }

        public String getBalanceStatus() {
            return balanceStatus;
        }

        public void setBalanceStatus(String balanceStatus) {
            this.balanceStatus = balanceStatus;
        }

        public String getDiagnosticMessage() {
            return diagnosticMessage;
        }

        public void setDiagnosticMessage(String diagnosticMessage) {
            this.diagnosticMessage = diagnosticMessage;
        }
    }

    public Long getMbomRevisionId() {
        return mbomRevisionId;
    }

    public void setMbomRevisionId(Long mbomRevisionId) {
        this.mbomRevisionId = mbomRevisionId;
    }

    public Boolean getIsBalanced() {
        return isBalanced;
    }

    public void setIsBalanced(Boolean balanced) {
        isBalanced = balanced;
    }

    public Integer getTotalEbomItemsCount() {
        return totalEbomItemsCount;
    }

    public void setTotalEbomItemsCount(Integer totalEbomItemsCount) {
        this.totalEbomItemsCount = totalEbomItemsCount;
    }

    public Integer getBalancedItemsCount() {
        return balancedItemsCount;
    }

    public void setBalancedItemsCount(Integer balancedItemsCount) {
        this.balancedItemsCount = balancedItemsCount;
    }

    public Integer getUnderConsumedCount() {
        return underConsumedCount;
    }

    public void setUnderConsumedCount(Integer underConsumedCount) {
        this.underConsumedCount = underConsumedCount;
    }

    public Integer getOverConsumedCount() {
        return overConsumedCount;
    }

    public void setOverConsumedCount(Integer overConsumedCount) {
        this.overConsumedCount = overConsumedCount;
    }

    public Integer getIllegalSourceCount() {
        return illegalSourceCount;
    }

    public void setIllegalSourceCount(Integer illegalSourceCount) {
        this.illegalSourceCount = illegalSourceCount;
    }

    public List<ItemBalanceDetail> getItemDetails() {
        return itemDetails;
    }

    public void setItemDetails(List<ItemBalanceDetail> itemDetails) {
        this.itemDetails = itemDetails;
    }

    public String getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }
}
