package com.ccdd.workflow.entity;

/**
 * M24: 会签投票策略枚举
 */
public enum SignStrategy {
    /**
     * 一票否决制 (必须全员通过)
     */
    UNANIMOUS("一票否决制"),

    /**
     * 比例通过制 (如赞成票 >= 80%)
     */
    PERCENTAGE("比例通过制"),

    /**
     * 首人决定制 (任意一人签署即流转)
     */
    FIRST_WINS("首人决定制");

    private final String description;

    SignStrategy(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
