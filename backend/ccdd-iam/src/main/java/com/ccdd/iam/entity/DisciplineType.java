package com.ccdd.iam.entity;

/**
 * 部门专业工程学科类型枚举
 * 支撑跨专业矩阵化协同与任务精准分发
 */
public enum DisciplineType {
    /**
     * 机械工程学科
     */
    MECHANICAL("机械结构总体"),

    /**
     * 电气工程学科
     */
    ELECTRICAL("数控电气与驱动"),

    /**
     * 控制工程学科
     */
    CONTROL("数控系统与伺服控制"),

    /**
     * 仿真分析学科
     */
    SIMULATION("数字化工程仿真"),

    /**
     * 液压气动学科
     */
    HYDRAULIC("液压润滑与排屑"),

    /**
     * 工艺工程学科
     */
    PROCESS("制造工艺与工装"),

    /**
     * 质量保障学科
     */
    QUALITY("整机质量与适航"),

    /**
     * 综合管理与IT学科
     */
    MANAGEMENT("综合管理与系统运维");

    private final String description;

    DisciplineType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
