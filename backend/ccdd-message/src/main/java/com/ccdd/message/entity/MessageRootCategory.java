package com.ccdd.message.entity;

/**
 * 消息根分类枚举
 */
public enum MessageRootCategory {
    /**
     * 人工普通邮件 (工程师点对点/抄送通信)
     */
    MANUAL("人工普通邮件"),

    /**
     * PLM 业务事件系统生成通知 (流程、任务、阶段门、变更等)
     */
    SYSTEM("系统业务通知");

    private final String description;

    MessageRootCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
