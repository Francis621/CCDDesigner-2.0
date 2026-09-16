package com.ccdd.manufacturing.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部 MES/ERP 逐项业务回执批量上报 DTO
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 5 节规约
 */
public class BatchReceiptReconciliationRequest {

    private String handoffBatchNo;
    private String externalTransactionId; // 外部 MES 批次会话凭证
    private List<LineReceiptItemDto> receipts = new ArrayList<>();

    public BatchReceiptReconciliationRequest() {
    }

    public BatchReceiptReconciliationRequest(String handoffBatchNo, String externalTransactionId, List<LineReceiptItemDto> receipts) {
        this.handoffBatchNo = handoffBatchNo;
        this.externalTransactionId = externalTransactionId;
        this.receipts = receipts;
    }

    public static class LineReceiptItemDto {
        private String lineItemNumber;
        private String materialNumber;
        private String externalReceiptNo; // MES 唯一的逐行确认流水号
        private String itemStatus; // ACCEPTED, REJECTED, PENDING
        private String assignedStorageBin; // 排产库位
        private String discrepancyMessage; // 若被驳回，车间详细原因

        public LineReceiptItemDto() {
        }

        public LineReceiptItemDto(String lineItemNumber, String materialNumber, String externalReceiptNo,
                                  String itemStatus, String assignedStorageBin, String discrepancyMessage) {
            this.lineItemNumber = lineItemNumber;
            this.materialNumber = materialNumber;
            this.externalReceiptNo = externalReceiptNo;
            this.itemStatus = itemStatus;
            this.assignedStorageBin = assignedStorageBin;
            this.discrepancyMessage = discrepancyMessage;
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
    }

    public String getHandoffBatchNo() {
        return handoffBatchNo;
    }

    public void setHandoffBatchNo(String handoffBatchNo) {
        this.handoffBatchNo = handoffBatchNo;
    }

    public String getExternalTransactionId() {
        return externalTransactionId;
    }

    public void setExternalTransactionId(String externalTransactionId) {
        this.externalTransactionId = externalTransactionId;
    }

    public List<LineReceiptItemDto> getReceipts() {
        return receipts;
    }

    public void setReceipts(List<LineReceiptItemDto> receipts) {
        this.receipts = receipts;
    }
}
