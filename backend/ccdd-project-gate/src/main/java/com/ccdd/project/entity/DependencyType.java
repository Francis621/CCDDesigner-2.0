package com.ccdd.project.entity;

/**
 * 任务前后置依赖关系类型枚举
 */
public enum DependencyType {
    FS("Finish-to-Start", "前置完成，后置方可开始"),
    SS("Start-to-Start", "前置开始，后置方可开始"),
    FF("Finish-to-Finish", "前置完成，后置方可完成"),
    SF("Start-to-Finish", "前置开始，后置方可完成");

    private final String code;
    private final String description;

    DependencyType(String code, String description) {
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
