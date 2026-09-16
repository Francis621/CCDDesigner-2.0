package com.ccdd.baseline.entity;

/**
 * 基线工程目的与多形态配置状态类型枚举 (M21 规格)
 */
public enum BaselinePurpose {
    REQUIREMENT_BASELINE("REQUIREMENT_BASELINE", "需求规格基线"),
    FUNCTIONAL_BASELINE("FUNCTIONAL_BASELINE", "功能基线 (FBL / 系统方案阶段)"),
    ALLOCATED_BASELINE("ALLOCATED_BASELINE", "分配基线 (ABL / 初步设计/PDR)"),
    PRODUCT_DESIGN_BASELINE("PRODUCT_DESIGN_BASELINE", "产品设计基线 (DBL / 关键设计评审/CDR)"),
    AS_DESIGNED("AS_DESIGNED", "订单工程设计基线 (100% EBOM)"),
    AS_PLANNED("AS_PLANNED", "工艺制造计划基线 (MBOM + BOP)"),
    AS_BUILT("AS_BUILT", "实物装配基线 (出厂实装/偏差履历)"),
    AS_DELIVERED("AS_DELIVERED", "客户交付验收基线 (法律交付凭据)"),
    AS_MAINTAINED("AS_MAINTAINED", "现场维保服役配置基线 (售后时序演进)");

    private final String code;
    private final String description;

    BaselinePurpose(String code, String description) {
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
