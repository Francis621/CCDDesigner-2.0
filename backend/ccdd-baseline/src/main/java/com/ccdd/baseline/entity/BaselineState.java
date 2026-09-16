package com.ccdd.baseline.entity;

/**
 * 基线生命周期状态枚举 (M21 规格与状态机)
 */
public enum BaselineState {
    DRAFT("DRAFT", "编制中/候选圈定中"),
    IN_REVIEW("IN_REVIEW", "闭包校验通过，处于审批流程中"),
    FROZEN("FROZEN", "已冻结生效 (物理级不可变，终态)"),
    SUPERSEDED("SUPERSEDED", "已被后继基线替代 (仍保留只读历史与溯源)");

    private final String code;
    private final String description;

    BaselineState(String code, String description) {
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
