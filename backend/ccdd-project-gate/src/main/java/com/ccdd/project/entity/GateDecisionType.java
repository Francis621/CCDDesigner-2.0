package com.ccdd.project.entity;

/**
 * 阶段门决策类型枚举 (M02 Section 10 & 40.4)
 */
public enum GateDecisionType {
    PASS("通过", "审查完全通过，允许进入下一阶段"),
    CONDITIONAL_PASS("有条件通过", "存在次要缺陷或次要证据缺口，必须附带明确放行范围与整改行动项"),
    REWORK("返工", "关键交付物不达标或核心证据失败，驳回当前阶段返工"),
    STOP("终止", "项目技术指标严重失真或商业价值丧失，冻结项目熔断终止");

    private final String label;
    private final String description;

    GateDecisionType(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
