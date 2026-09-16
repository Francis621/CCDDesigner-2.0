package com.ccdd.message.dto;

import com.ccdd.message.entity.MessageRootCategory;

import java.io.Serializable;
import java.time.Instant;

/**
 * 发送邮件成功响应 DTO (对齐 OpenAPI 8.2)
 */
public class SendMessageResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private MessageRootCategory rootCategory;
    private String status;
    private Integer deliveredCount;
    private Instant sentAt;

    public SendMessageResponse() {
    }

    public SendMessageResponse(Long messageId, String status, Integer deliveredCount, Instant sentAt) {
        this.messageId = messageId;
        this.status = status;
        this.deliveredCount = deliveredCount;
        this.sentAt = sentAt;
    }

    public SendMessageResponse(Long messageId, MessageRootCategory rootCategory, Integer deliveredCount, String status, Instant sentAt) {
        this.messageId = messageId;
        this.rootCategory = rootCategory;
        this.deliveredCount = deliveredCount;
        this.status = status;
        this.sentAt = sentAt;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public MessageRootCategory getRootCategory() {
        return rootCategory;
    }

    public void setRootCategory(MessageRootCategory rootCategory) {
        this.rootCategory = rootCategory;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDeliveredCount() {
        return deliveredCount;
    }

    public void setDeliveredCount(Integer deliveredCount) {
        this.deliveredCount = deliveredCount;
    }

    public Integer getRecipientCount() {
        return deliveredCount;
    }

    public void setRecipientCount(Integer recipientCount) {
        this.deliveredCount = recipientCount;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }
}
