package com.ccdd.iam.entity;

/**
 * 专业工程资质认证类型枚举 (支撑 SoD-02 与高阶审批资质)
 */
public enum QualificationType {
    /**
     * 验证结论专职审查员 (拥有签署 PASS 资格，支撑 SoD-02)
     */
    VERIFICATION_REVIEWER("验证结论专职审查员"),

    /**
     * 首席系统架构师
     */
    LEAD_SYSTEM_ARCHITECT("首席系统架构师"),

    /**
     * 质量总监 (具备作废 WITHDRAWN 特批权)
     */
    CHIEF_QUALITY_OFFICER("质量总监"),

    /**
     * 安全关键审查员
     */
    SAFETY_ENGINEER("安全关键审查员");

    private final String description;

    QualificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
