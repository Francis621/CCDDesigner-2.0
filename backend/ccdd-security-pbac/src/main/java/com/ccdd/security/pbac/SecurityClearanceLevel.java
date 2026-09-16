package com.ccdd.security.pbac;

/**
 * 工程安全密级等级枚举 (Bell-LaPadula 安全模型落地)
 */
public enum SecurityClearanceLevel {

    PUBLIC(1, "公开"),
    INTERNAL(2, "内部"),
    CONFIDENTIAL(3, "机密"),
    STRICTLY_CONFIDENTIAL(4, "绝密");

    private final int level;
    private final String description;

    SecurityClearanceLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static SecurityClearanceLevel fromString(String str) {
        if (str == null) return INTERNAL;
        for (SecurityClearanceLevel s : values()) {
            if (s.name().equalsIgnoreCase(str)) {
                return s;
            }
        }
        return INTERNAL;
    }
}
