package com.ccdd.message.entity;

/**
 * 业务系统消息子分类枚举
 * 对齐规格说明书 §3.1 消息分类明细字典
 */
public enum SystemMessageType {
    /**
     * 工作流审批通知 (流程发起、待办加签、会签流转)
     */
    WORKFLOW("工作流审批"),

    /**
     * 流程模板与规则变更通知
     */
    WORKFLOW_CHANGE("流程模板变更"),

    /**
     * 研发任务分配/延期/提交通知
     */
    TASK("研发任务通知"),

    /**
     * 项目阶段门/技术评审状态决议通知
     */
    REVIEW("阶段门与技术评审"),

    /**
     * 项目阶段门关键倒计时与告警通知
     */
    GATE("项目阶段门通知"),

    /**
     * 工程问题/ECR/ECO 变更指令与处置通知
     */
    CHANGE("工程变更通知"),

    /**
     * 文档/图纸修订发布与轻量化就绪通知
     */
    DOCUMENT("图文档发布"),

    /**
     * 150% BOM 规则与 EBOM/MBOM 结构变更通知
     */
    BOM("BOM结构变更"),

    /**
     * 阶段基线固化与冻结广播通知
     */
    BASELINE("工程基线冻结"),

    /**
     * 协同设计协作批注与@提醒通知
     */
    COMMENT("协同批注@提醒"),

    /**
     * 平台通用系统通知
     */
    GENERIC("系统通用通知");

    private final String description;

    SystemMessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
