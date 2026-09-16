package com.ccdd.iam.entity;

/**
 * 项目工作组角色枚举
 * 与全局行政职能角色彻底解耦，界定具体研制项目内的操作特权
 */
public enum ProjectRoleType {
    /**
     * 项目负责人 / 总师
     */
    PROJECT_LEAD("项目负责人"),

    /**
     * 主设人员 / 开发者
     */
    DESIGNER("主设人员"),

    /**
     * 校验核对员
     */
    CHECKER("校验核对员"),

    /**
     * 审批签署人
     */
    APPROVER("审批签署人"),

    /**
     * 访客观察员 (只读查看)
     */
    GUEST("访客观察员");

    private final String description;

    ProjectRoleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
