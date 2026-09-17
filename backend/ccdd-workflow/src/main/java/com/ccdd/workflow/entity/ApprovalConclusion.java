package com.ccdd.workflow.entity;

/**
 * M24: 审批最终结论枚举
 */
public enum ApprovalConclusion {
    /**
     * 审查全票或达标通过
     */
    APPROVED("通过"),

    /**
     * 审查驳回不通过 (打回草稿)
     */
    REJECTED("驳回"),

    /**
     * 发起人主动撤回
     */
    WITHDRAWN("已撤回");

    private final String description;

    ApprovalConclusion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
