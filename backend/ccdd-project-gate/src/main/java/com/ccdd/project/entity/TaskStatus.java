package com.ccdd.project.entity;

/**
 * 任务执行状态枚举 (技术进度维度，与交付物审批、阶段门决策彻底解耦独立)
 */
public enum TaskStatus {
    NOT_STARTED("未启动"),
    IN_PROGRESS("进行中"),
    SUBMITTED("已提交"),
    COMPLETED("已完成"),
    BLOCKED("遇阻挂起"),
    CANCELLED("已取消");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
