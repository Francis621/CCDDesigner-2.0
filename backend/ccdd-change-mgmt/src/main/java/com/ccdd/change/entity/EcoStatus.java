package com.ccdd.change.entity;

/**
 * M22 工程变更实施单 (ECO) 业务状态枚举
 * 落实两阶段解耦原则：RELEASED 仅代表设计完成，CLOSED 代表现场实施回执 100% 确认
 */
public enum EcoStatus {
    DRAFT("DRAFT", "编制中"),
    ASSESSING("ASSESSING", "影响分析与专业处置裁决中"),
    AUTHORIZED("AUTHORIZED", "CCB 正式授权实施新版本"),
    IMPLEMENTING("IMPLEMENTING", "新版本设计工作区迭代中"),
    REVIEWING("REVIEWING", "新设计与再验证报告会签中"),
    RELEASED("RELEASED", "设计新版本发布 (PLM设计态终结，禁止直接关闭)"),
    EXECUTING("EXECUTING", "现场与工厂物理实施对账中"),
    CLOSED("CLOSED", "现场回执 100% 确认，整单法律关闭"),
    CANCELLED("CANCELLED", "中途取消");

    private final String code;
    private final String description;

    EcoStatus(String code, String description) {
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
