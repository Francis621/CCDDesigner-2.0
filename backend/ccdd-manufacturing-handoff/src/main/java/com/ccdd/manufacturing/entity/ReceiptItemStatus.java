package com.ccdd.manufacturing.entity;

/**
 * 逐项业务回执状态枚举
 * 遵循《CCD-DEV-SPEC-2.0-D08 专项规格包》第 5.1 节规约
 */
public enum ReceiptItemStatus {
    /**
     * 车间系统已成功接收、校验并排产此行物料
     */
    ACCEPTED("收讫接受"),

    /**
     * 车间系统驳回此行物料 (如物料主数据不存在、库位被锁、工序无效)
     */
    REJECTED("业务驳回"),

    /**
     * 外部系统正在异步核算处理中
     */
    PENDING("处理中");

    private final String description;

    ReceiptItemStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
