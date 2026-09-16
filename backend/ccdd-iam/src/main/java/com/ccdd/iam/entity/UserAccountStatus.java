package com.ccdd.iam.entity;

/**
 * 用户账号状态枚举
 */
public enum UserAccountStatus {
    /**
     * 正常激活
     */
    ACTIVE("正常激活"),

    /**
     * 临时冻结
     */
    SUSPENDED("临时冻结"),

    /**
     * 离职禁用
     */
    DEACTIVATED("离职禁用"),

    /**
     * 安全风控锁定
     */
    LOCKED("安全风控锁定");

    private final String description;

    UserAccountStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
