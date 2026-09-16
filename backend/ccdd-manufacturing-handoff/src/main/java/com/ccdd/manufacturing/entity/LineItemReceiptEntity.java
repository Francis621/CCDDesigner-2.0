package com.ccdd.manufacturing.entity;

import java.time.Instant;

/**
 * 逐项业务回执与对账明细实体
 * 映射物理表 sys_line_item_receipts
 */
public class LineItemReceiptEntity {

    private Long receiptId;
    private String tenantId;
    private Long packageId;
    private String lineItemNumber;
    private String materialNumber;
    private String externalReceiptNo;
    private ReceiptItemStatus itemStatus;
    private String assignedStorageBin;
    private String discrepancyMessage;
    private Instant receivedAt;

    public LineItemReceiptEntity() {
    }

    public LineItemReceiptEntity(Long receiptId, String tenantId, Long packageId, String lineItemNumber,
                                 String materialNumber, String externalReceiptNo, ReceiptItemStatus itemStatus,
                                 String assignedStorageBin, String discrepancyMessage, Instant receivedAt) {
        this.receiptId = receiptId;
        this.tenantId = tenantId;
        this.packageId = packageId;
        this.lineItemNumber = lineItemNumber;
        this.materialNumber = materialNumber;
        this.externalReceiptNo = externalReceiptNo;
        this.itemStatus = itemStatus;
        this.assignedStorageBin = assignedStorageBin;
        this.discrepancyMessage = discrepancyMessage;
        this.receivedAt = receivedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long receiptId;
        private String tenantId;
        private Long packageId;
        private String lineItemNumber;
        private String materialNumber;
        private String externalReceiptNo;
        private ReceiptItemStatus itemStatus = ReceiptItemStatus.PENDING;
        private String assignedStorageBin;
        private String discrepancyMessage;
        private Instant receivedAt = Instant.now();

        public Builder receiptId(Long receiptId) {
            this.receiptId = receiptId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder packageId(Long packageId) {
            this.packageId = packageId;
            return this;
        }

        public Builder lineItemNumber(String lineItemNumber) {
            this.lineItemNumber = lineItemNumber;
            return this;
        }

        public Builder materialNumber(String materialNumber) {
            this.materialNumber = materialNumber;
            return this;
        }

        public Builder externalReceiptNo(String externalReceiptNo) {
            this.externalReceiptNo = externalReceiptNo;
            return this;
        }

        public Builder itemStatus(ReceiptItemStatus itemStatus) {
            this.itemStatus = itemStatus;
            return this;
        }

        public Builder assignedStorageBin(String assignedStorageBin) {
            this.assignedStorageBin = assignedStorageBin;
            return this;
        }

        public Builder discrepancyMessage(String discrepancyMessage) {
            this.discrepancyMessage = discrepancyMessage;
            return this;
        }

        public Builder receivedAt(Instant receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public LineItemReceiptEntity build() {
            return new LineItemReceiptEntity(receiptId, tenantId, packageId, lineItemNumber,
                    materialNumber, externalReceiptNo, itemStatus, assignedStorageBin,
                    discrepancyMessage, receivedAt);
        }
    }

    public Long getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(Long receiptId) {
        this.receiptId = receiptId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Long getPackageId() {
        return packageId;
    }

    public void setPackageId(Long packageId) {
        this.packageId = packageId;
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

    public ReceiptItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ReceiptItemStatus itemStatus) {
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
