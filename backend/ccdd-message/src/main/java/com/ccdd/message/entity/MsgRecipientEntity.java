package com.ccdd.message.entity;

import java.io.Serializable;

/**
 * 消息接收人实体 (对应 plm_msg.msg_recipient)
 */
public class MsgRecipientEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recipientId;
    private Long messageId;
    private String userId;
    private String userName;
    private String recipientType; // TO / CC / BCC

    public MsgRecipientEntity() {
    }

    public MsgRecipientEntity(Long recipientId, Long messageId, String userId, String userName, String recipientType) {
        this.recipientId = recipientId;
        this.messageId = messageId;
        this.userId = userId;
        this.userName = userName;
        this.recipientType = recipientType;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(String recipientType) {
        this.recipientType = recipientType;
    }
}
