package com.ccdd.bom.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 解算派生的 100% 实例 BOM 行 DTO
 * 包含变体物理信息、计算后的确切数量与规则选用溯源凭证
 */
public class ResolvedBomLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private String slotId;
    private String slotName;
    private String childPartNumber;
    private String childPartName;
    private Double quantity;
    private String matchedRule;
    private List<String> triggerVariables;
    private Map<String, Object> attributes;

    public ResolvedBomLine() {
    }

    public ResolvedBomLine(String slotId, String slotName, String childPartNumber, String childPartName,
                           Double quantity, String matchedRule, List<String> triggerVariables,
                           Map<String, Object> attributes) {
        this.slotId = slotId;
        this.slotName = slotName;
        this.childPartNumber = childPartNumber;
        this.childPartName = childPartName;
        this.quantity = quantity;
        this.matchedRule = matchedRule;
        this.triggerVariables = triggerVariables;
        this.attributes = attributes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String slotId;
        private String slotName;
        private String childPartNumber;
        private String childPartName;
        private Double quantity;
        private String matchedRule;
        private List<String> triggerVariables;
        private Map<String, Object> attributes;

        public Builder slotId(String slotId) { this.slotId = slotId; return this; }
        public Builder slotName(String slotName) { this.slotName = slotName; return this; }
        public Builder childPartNumber(String childPartNumber) { this.childPartNumber = childPartNumber; return this; }
        public Builder childPartName(String childPartName) { this.childPartName = childPartName; return this; }
        public Builder quantity(Double quantity) { this.quantity = quantity; return this; }
        public Builder matchedRule(String matchedRule) { this.matchedRule = matchedRule; return this; }
        public Builder triggerVariables(List<String> triggerVariables) { this.triggerVariables = triggerVariables; return this; }
        public Builder attributes(Map<String, Object> attributes) { this.attributes = attributes; return this; }

        public ResolvedBomLine build() {
            return new ResolvedBomLine(slotId, slotName, childPartNumber, childPartName, quantity, matchedRule, triggerVariables, attributes);
        }
    }

    public String getSlotId() { return slotId; }
    public void setSlotId(String slotId) { this.slotId = slotId; }

    public String getSlotName() { return slotName; }
    public void setSlotName(String slotName) { this.slotName = slotName; }

    public String getChildPartNumber() { return childPartNumber; }
    public void setChildPartNumber(String childPartNumber) { this.childPartNumber = childPartNumber; }

    public String getChildPartName() { return childPartName; }
    public void setChildPartName(String childPartName) { this.childPartName = childPartName; }

    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }

    public String getMatchedRule() { return matchedRule; }
    public void setMatchedRule(String matchedRule) { this.matchedRule = matchedRule; }

    public List<String> getTriggerVariables() { return triggerVariables; }
    public void setTriggerVariables(List<String> triggerVariables) { this.triggerVariables = triggerVariables; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}
