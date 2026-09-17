package com.ccdd.workflow.entity;

/**
 * M24: 流程实例运行状态枚举
 */
public enum InstanceStatus {
    /**
     * 流程正在流转中
     */
    RUNNING("流转中"),

    /**
     * 流程正常结束并达成决议
     */
    COMPLETED("已完成"),

    /**
     * 因内容篡改、手动撤回或异常被强行终止
     */
    TERMINATED("已强行终止"),

    /**
     * 管理员挂起
     */
    SUSPENDED("已挂起");

    private final String description;

    InstanceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
