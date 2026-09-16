package com.ccdd.change.entity;

/**
 * M22 工程变更原因分类枚举
 */
public enum ChangeReasonType {
    CUSTOMER_REQUIREMENT("CUSTOMER_REQUIREMENT", "客户提出新需求/技术规格调整"),
    FIELD_FAILURE("FIELD_FAILURE", "现场维保/实机故障反馈 (M28反馈)"),
    SIMULATION_DEVIATION("SIMULATION_DEVIATION", "虚拟验证未达标/性能超差 (M10/M11发现)"),
    MANUFACTURING_DEFECT("MANUFACTURING_DEFECT", "车间制造装配工艺性缺陷 (M25/M26反馈)"),
    COST_REDUCTION("COST_REDUCTION", "价值工程/降本重构"),
    SUPPLIER_OBSOLESCENCE("SUPPLIER_OBSOLESCENCE", "供应商停产/元器件升级"),
    STANDARDS_COMPLIANCE("STANDARDS_COMPLIANCE", "行业标准/适航法规变更");

    private final String code;
    private final String description;

    ChangeReasonType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
