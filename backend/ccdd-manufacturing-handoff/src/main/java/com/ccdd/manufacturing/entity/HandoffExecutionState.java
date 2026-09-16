package com.ccdd.manufacturing.entity;

/**
 * 制造下发批次包执行状态机枚举
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 4.2 节规约
 */
public enum HandoffExecutionState {
    /**
     * 草稿新建态
     */
    DRAFT("草稿新建"),

    /**
     * 预检通过就绪下发态
     */
    READY_TO_SEND("就绪下发"),

    /**
     * 正在调用外部接口传输态
     */
    TRANSMITTING("传输中"),

    /**
     * 收到网络 ACK (HTTP 200) - 注意：HTTP 200 仅代表通信收到，不代表业务消费成功！
     */
    ACKNOWLEDGED("通信收讫"),

    /**
     * 全部逐项业务回执收讫成功 (所有明细状态均为 ACCEPTED)，下发闭环完成
     */
    RECONCILED_CONFIRMED("对账确认闭环"),

    /**
     * 部分明细项通过，部分被车间驳回 (存在待纠偏行)
     */
    PARTIALLY_ACCEPTED("部分接受"),

    /**
     * 生产系统整单业务驳回 (如库位封锁、工厂编码无效)
     */
    REJECTED("业务驳回");

    private final String description;

    HandoffExecutionState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
