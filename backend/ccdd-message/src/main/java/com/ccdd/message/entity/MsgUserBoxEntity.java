package com.ccdd.message.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 用户个人信箱明细实体 (承载个人箱体、已读、星标与归档/垃圾箱状态，对应 plm_msg.msg_user_box)
 */
public class MsgUserBoxEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userBoxId;
    private String userId;
    private Long messageId;
    private MailboxBoxType boxType;
    private Boolean isRead;
    private Boolean isStarred;
    private Boolean isArchived;
    private Boolean isTrash;
    private Instant createdAt;

    public MsgUserBoxEntity() {
    }

    public MsgUserBoxEntity(Long userBoxId, String userId, Long messageId, MailboxBoxType boxType,
                            Boolean isRead, Boolean isStarred, Boolean isArchived, Boolean isTrash, Instant createdAt) {
        this.userBoxId = userBoxId;
        this.userId = userId;
        this.messageId = messageId;
        this.boxType = boxType;
        this.isRead = isRead;
        this.isStarred = isStarred;
        this.isArchived = isArchived;
        this.isTrash = isTrash;
        this.createdAt = createdAt;
    }

    public Long getUserBoxId() {
        return userBoxId;
    }

    public void setUserBoxId(Long userBoxId) {
        this.userBoxId = userBoxId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public MailboxBoxType getBoxType() {
        return boxType;
    }

    public void setBoxType(MailboxBoxType boxType) {
        this.boxType = boxType;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
