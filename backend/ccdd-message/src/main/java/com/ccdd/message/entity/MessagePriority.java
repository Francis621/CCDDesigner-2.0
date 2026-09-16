package com.ccdd.message.entity;

/**
 * 消息优先级枚举
 */
public enum MessagePriority {
    /**
     * 低优先级
     */
    LOW("低"),

    /**
     * 普通优先级
     */
    NORMAL("普通"),

    /**
     * 高优先级
     */
    HIGH("高"),

    /**
     * 紧急阻断级
     */
    URGENT("紧急");

    private final String description;

    MessagePriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
