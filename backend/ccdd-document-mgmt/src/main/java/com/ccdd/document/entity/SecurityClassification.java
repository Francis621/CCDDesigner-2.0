package com.ccdd.document.entity;

/**
 * 文档安全密级枚举 (M19 规格与 PBAC 联动)
 */
public enum SecurityClassification {
    PUBLIC("PUBLIC", "公开"),
    INTERNAL("INTERNAL", "内部受控"),
    CONFIDENTIAL("CONFIDENTIAL", "机密/核心技术"),
    RESTRICTED("RESTRICTED", "绝密/核心机密与关键参数");

    private final String code;
    private final String description;

    SecurityClassification(String code, String description) {
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
