package com.ccdd.bom.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 150% 可配置超级结构 BOM 明细行实体 (映射 sys_configurable_bom_lines 表)
 * 挂载标准化槽位（Slot）、变体关联、选用条件 DSL 与数量公式
 */
public class ConfigurableBomLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long lineId;
    private String tenantId;
    private Long revisionId;
    private Long parentLineId;
    private Integer lineNumber;
    private String slotId;
    private String slotName;
    private String cardinality; // '1..1', '0..1', '1..N'
    private String childPartRevId;
    private String childPartNumber;
    private String childPartName;
    private String selectionRule;   // 选用条件 DSL (如: $SPINDLE_TYPE == "BT40")
    private String quantityFormula; // 数量计算公式 (如: 1 或 $TOOL_CAPACITY)
    private Boolean isPhantom;
    private Instant createdAt;

    public ConfigurableBomLine() {
    }

    public ConfigurableBomLine(Long lineId, String tenantId, Long revisionId, Long parentLineId,
                               Integer lineNumber, String slotId, String slotName, String cardinality,
                               String childPartRevId, String childPartNumber, String childPartName,
                               String selectionRule, String quantityFormula, Boolean isPhantom,
                               Instant createdAt) {
        this.lineId = lineId;
        this.tenantId = tenantId;
        this.revisionId = revisionId;
        this.parentLineId = parentLineId;
        this.lineNumber = lineNumber;
        this.slotId = slotId;
        this.slotName = slotName;
        this.cardinality = cardinality;
        this.childPartRevId = childPartRevId;
        this.childPartNumber = childPartNumber;
        this.childPartName = childPartName;
        this.selectionRule = selectionRule;
        this.quantityFormula = quantityFormula;
        this.isPhantom = isPhantom;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long lineId;
        private String tenantId;
        private Long revisionId;
        private Long parentLineId;
        private Integer lineNumber;
        private String slotId;
        private String slotName;
        private String cardinality;
        private String childPartRevId;
        private String childPartNumber;
        private String childPartName;
        private String selectionRule;
        private String quantityFormula;
        private Boolean isPhantom;
        private Instant createdAt;

        public Builder lineId(Long lineId) { this.lineId = lineId; return this; }
        public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
        public Builder revisionId(Long revisionId) { this.revisionId = revisionId; return this; }
        public Builder parentLineId(Long parentLineId) { this.parentLineId = parentLineId; return this; }
        public Builder lineNumber(Integer lineNumber) { this.lineNumber = lineNumber; return this; }
        public Builder slotId(String slotId) { this.slotId = slotId; return this; }
        public Builder slotName(String slotName) { this.slotName = slotName; return this; }
        public Builder cardinality(String cardinality) { this.cardinality = cardinality; return this; }
        public Builder childPartRevId(String childPartRevId) { this.childPartRevId = childPartRevId; return this; }
        public Builder childPartNumber(String childPartNumber) { this.childPartNumber = childPartNumber; return this; }
        public Builder childPartName(String childPartName) { this.childPartName = childPartName; return this; }
        public Builder selectionRule(String selectionRule) { this.selectionRule = selectionRule; return this; }
        public Builder quantityFormula(String quantityFormula) { this.quantityFormula = quantityFormula; return this; }
        public Builder isPhantom(Boolean isPhantom) { this.isPhantom = isPhantom; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ConfigurableBomLine build() {
            return new ConfigurableBomLine(lineId, tenantId, revisionId, parentLineId, lineNumber,
                    slotId, slotName, cardinality, childPartRevId, childPartNumber, childPartName,
                    selectionRule, quantityFormula, isPhantom, createdAt);
        }
    }

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public Long getRevisionId() { return revisionId; }
    public void setRevisionId(Long revisionId) { this.revisionId = revisionId; }

    public Long getParentLineId() { return parentLineId; }
    public void setParentLineId(Long parentLineId) { this.parentLineId = parentLineId; }

    public Integer getLineNumber() { return lineNumber; }
    public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotName() { return slotName; }
    public void setSlotName(String slotName) { this.slotName = slotName; }

    public String getCardinality() { return cardinality; }
    public void setCardinality(String cardinality) { this.cardinality = cardinality; }

    public String getChildPartRevId() { return childPartRevId; }
    public void setChildPartRevId(String childPartRevId) { this.childPartRevId = childPartRevId; }

    public String getChildPartNumber() { return childPartNumber; }
    public void setChildPartNumber(String childPartNumber) { this.childPartNumber = childPartNumber; }

    public String getChildPartName() { return childPartName; }
    public void setChildPartName(String childPartName) { this.childPartName = childPartName; }

    public String getSelectionRule() { return selectionRule; }
    public void setSelectionRule(String selectionRule) { this.selectionRule = selectionRule; }

    public String getQuantityFormula() { return quantityFormula; }
    public void setQuantityFormula(String quantityFormula) { this.quantityFormula = quantityFormula; }

    public Boolean getIsPhantom() { return isPhantom; }
    public void setIsPhantom(Boolean isPhantom) { this.isPhantom = isPhantom; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
