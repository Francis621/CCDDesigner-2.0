package com.ccdd.message.dto;

import java.io.Serializable;

/**
 * 更新用户信箱条目状态请求（已读、标星、移动箱体等）
 */
public class UpdateMessageStatusRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 是否加星标
     */
    private Boolean isStarred;

    /**
     * 是否归档
     */
    private Boolean isArchived;

    /**
     * 是否放入废纸篓
     */
    private Boolean isTrash;

    /**
     * 目标箱体（INBOX / OUTBOX / ARCHIVE / TRASH）
     */
    private String targetBoxType;

    public UpdateMessageStatusRequest() {
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public Boolean getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(Boolean starred) {
        isStarred = starred;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean archived) {
        isArchived = archived;
    }

    public Boolean getIsTrash() {
        return isTrash;
    }

    public void setIsTrash(Boolean trash) {
        isTrash = trash;
    }

    public String getTargetBoxType() {
        return targetBoxType;
    }

    public void setTargetBoxType(String targetBoxType) {
        this.targetBoxType = targetBoxType;
    }
}
