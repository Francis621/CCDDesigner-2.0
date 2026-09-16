package com.ccdd.change.entity;

/**
 * M22 工程变更请求 (ECR) 业务状态枚举
 */
public enum EcrStatus {
    DRAFT("DRAFT", "编制中"),
    SUBMITTED("SUBMITTED", "已提交/技术可行性初审中"),
    IN_REVIEW("IN_REVIEW", "变更委员会 (CCB) 评审中"),
    APPROVED("APPROVED", "批准立项 (授权签发 ECO)"),
    REJECTED("REJECTED", "驳回终止"),
    CLOSED("CLOSED", "关联的 ECO 已全闭环后关闭");

    private final String code;
    private final String description;

    EcrStatus(String code, String description) {
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
