package com.ccdd.change.entity;

/**
 * M22 候选影响处置决定枚举 (AT-10 规范)
 */
public enum ImpactDecisionType {
    MODIFY("MODIFY", "确认受波及且必须升版/新建设计"),
    RE_VERIFY("RE_VERIFY", "结构不变，但历史验证失效，需重新计算/试验"),
    REVIEW_ONLY("REVIEW_ONLY", "确认受波及，仅需设计校核，无需修改实体"),
    NO_IMPACT("NO_IMPACT", "经专业分析判定无实际影响 (必须填写工程免责依据)");

    private final String code;
    private final String description;

    ImpactDecisionType(String code, String description) {
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
