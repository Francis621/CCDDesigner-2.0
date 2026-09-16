package com.ccdd.message.entity;

/**
 * 个人邮箱箱体枚举
 */
public enum MailboxBoxType {
    /**
     * 收件箱
     */
    INBOX("收件箱"),

    /**
     * 发件箱 (已发送)
     */
    OUTBOX("已发送"),

    /**
     * 草稿箱
     */
    DRAFT("草稿箱"),

    /**
     * 已归档
     */
    ARCHIVE("已归档"),

    /**
     * 废纸篓 / 垃圾箱
     */
    TRASH("废纸篓");

    private final String description;

    MailboxBoxType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
