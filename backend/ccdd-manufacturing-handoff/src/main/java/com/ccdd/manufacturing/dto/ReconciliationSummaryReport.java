package com.ccdd.manufacturing.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 逐项对账结果汇总报告 DTO
 */
public class ReconciliationSummaryReport {

    private String handoffBatchNo;
    private Long packageId;
    private String executionState; // RECONCILED_CONFIRMED, PARTIALLY_ACCEPTED, REJECTED, ACKNOWLEDGED
    private Integer totalLines;
    private Integer acceptedLines;
    private Integer rejectedLines;
    private Boolean isFullyReconciled;
    private Instant reconciledAt;
    private List<ReconciliationLineItemDetail> lineDetails = new ArrayList<>();

    public ReconciliationSummaryReport() {
    }

    public ReconciliationSummaryReport(String handoffBatchNo, Long packageId, String executionState,
                                       Integer totalLines, Integer acceptedLines, Integer rejectedLines,
                                       Boolean isFullyReconciled, Instant reconciledAt,
                                       List<ReconciliationLineItemDetail> lineDetails) {
        this.handoffBatchNo = handoffBatchNo;
        this.packageId = packageId;
        this.executionState = executionState;
        this.totalLines = totalLines;
        this.acceptedLines = acceptedLines;
        this.rejectedLines = rejectedLines;
        this.isFullyReconciled = isFullyReconciled;
        this.reconciledAt = reconciledAt;
        this.lineDetails = lineDetails;
    }

    public static class ReconciliationLineItemDetail {
        private String lineItemNumber;
        private String materialNumber;
        private String externalReceiptNo;
        private String itemStatus;
        private String assignedStorageBin;
        private String discrepancyMessage;
        private Instant receivedAt;

        public ReconciliationLineItemDetail() {
        }

        public ReconciliationLineItemDetail(String lineItemNumber, String materialNumber, String externalReceiptNo,
                                            String itemStatus, String assignedStorageBin, String discrepancyMessage,
                                            Instant receivedAt) {
            this.lineItemNumber = lineItemNumber;
            this.materialNumber = materialNumber;
            this.externalReceiptNo = externalReceiptNo;
            this.itemStatus = itemStatus;
            this.assignedStorageBin = assignedStorageBin;
            this.discrepancyMessage = discrepancyMessage;
            this.receivedAt = receivedAt;
        }

        public String getLineItemNumber() {
            return lineItemNumber;
        }

        public void setLineItemNumber(String lineItemNumber) {
            this.lineItemNumber = lineItemNumber;
        }

        public String getMaterialNumber() {
            return materialNumber;
        }

        public void setMaterialNumber(String materialNumber) {
            this.materialNumber = materialNumber;
        }

        public String getExternalReceiptNo() {
            return externalReceiptNo;
        }

        public void setExternalReceiptNo(String externalReceiptNo) {
            this.externalReceiptNo = externalReceiptNo;
        }

        public String getItemStatus() {
            return itemStatus;
        }

        public void setItemStatus(String itemStatus) {
            this.itemStatus = itemStatus;
        }

        public String getAssignedStorageBin() {
            return assignedStorageBin;
        }

        public void setAssignedStorageBin(String assignedStorageBin) {
            this.assignedStorageBin = assignedStorageBin;
        }

        public String getDiscrepancyMessage() {
            return discrepancyMessage;
        }

        public void setDiscrepancyMessage(String discrepancyMessage) {
            this.discrepancyMessage = discrepancyMessage;
        }

        public Instant getReceivedAt() {
            return receivedAt;
        }

        public void setReceivedAt(Instant receivedAt) {
            this.receivedAt = receivedAt;
        }
    }

    public String getHandoffBatchNo() {
        return handoffBatchNo;
    }

    public void setHandoffBatchNo(String handoffBatchNo) {
        this.handoffBatchNo = handoffBatchNo;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }

    public String getExecutionState() {
        return executionState;
    }

    public void setExecutionState(String executionState) {
        this.executionState = executionState;
    }

    public Integer getTotalLines() {
        return totalLines;
    }

    public void setTotalLines(Integer totalLines) {
        this.totalLines = totalLines;
    }

    public Integer getAcceptedLines() {
        return acceptedLines;
    }

    public void setAcceptedLines(Integer acceptedLines) {
        this.acceptedLines = acceptedLines;
    }

    public Integer getRejectedLines() {
        return rejectedLines;
    }

    public void setRejectedLines(Integer rejectedLines) {
        this.rejectedLines = rejectedLines;
    }

    public Boolean getIsFullyReconciled() {
        return isFullyReconciled;
    }

    public void setIsFullyReconciled(Boolean fullyReconciled) {
        isFullyReconciled = fullyReconciled;
    }

    public Instant getReconciledAt() {
        return reconciledAt;
    }

    public void setReconciledAt(Instant reconciledAt) {
        this.reconciledAt = reconciledAt;
    }

    public List<ReconciliationLineItemDetail> getLineDetails() {
        return lineDetails;
    }

    public void setLineDetails(List<ReconciliationLineItemDetail> lineDetails) {
        this.lineDetails = lineDetails;
    }
}
