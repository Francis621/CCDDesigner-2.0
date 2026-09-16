package com.ccdd.message.dto;

import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.entity.SystemMessageType;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户邮箱列表项 DTO (消息摘要卡片)
 */
public class MailboxItemDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userBoxId;
    private Long messageId;
    private MessageRootCategory rootCategory;
    private SystemMessageType systemType;
    private String subject;
    private String senderUserId;
    private String senderUserName;
    private MessagePriority priority;
    private Boolean isRead;
    private Boolean isStarred;
    private Boolean isArchived;
    private Boolean hasAttachment;
    private List<String> customTags = new ArrayList<>();

    private String relatedProjectId;
    private String relatedObjType;
    private String relatedObjId;
    private String targetActionUrl;
    private Instant createdAt;

    public MailboxItemDto() {
    }

    public Long getUserBoxId() {
        return userBoxId;
    }

    public void setUserBoxId(Long userBoxId) {
        this.userBoxId = userBoxId;
    }

    public Long getItemId() {
        return userBoxId;
    }

    public void setItemId(Long itemId) {
        this.userBoxId = itemId;
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

    public SystemMessageType getSystemType() {
        return systemType;
    }

    public void setSystemType(SystemMessageType systemType) {
        this.systemType = systemType;
    }

    public SystemMessageType getSubType() {
        return systemType;
    }

    public void setSubType(SystemMessageType subType) {
        this.systemType = subType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderId() {
        return senderUserId;
    }

    public void setSenderId(String senderId) {
        this.senderUserId = senderId;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
    }

    public String getSenderDisplayName() {
        return senderUserName;
    }

    public void setSenderDisplayName(String senderDisplayName) {
        this.senderUserName = senderDisplayName;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
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

    public Boolean getHasAttachment() {
        return hasAttachment;
    }

    public void setHasAttachment(Boolean hasAttachment) {
        this.hasAttachment = hasAttachment;
    }

    public List<String> getCustomTags() {
        return customTags;
    }

    public void setCustomTags(List<String> customTags) {
        this.customTags = customTags;
    }

    public String getRelatedProjectId() {
        return relatedProjectId;
    }

    public void setRelatedProjectId(String relatedProjectId) {
        this.relatedProjectId = relatedProjectId;
    }

    public String getRelatedObjType() {
        return relatedObjType;
    }

    public void setRelatedObjType(String relatedObjType) {
        this.relatedObjType = relatedObjType;
    }

    public String getRelatedObjectType() {
        return relatedObjType;
    }

    public void setRelatedObjectType(String relatedObjectType) {
        this.relatedObjType = relatedObjectType;
    }

    public String getRelatedObjId() {
        return relatedObjId;
    }

    public void setRelatedObjId(String relatedObjId) {
        this.relatedObjId = relatedObjId;
    }

    public Long getRelatedObjectId() {
        try {
            return relatedObjId != null ? Long.parseLong(relatedObjId) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void setRelatedObjectId(Long relatedObjectId) {
        this.relatedObjId = relatedObjectId != null ? String.valueOf(relatedObjectId) : null;
    }

    public String getTargetActionUrl() {
        return targetActionUrl;
    }

    public void setTargetActionUrl(String targetActionUrl) {
        this.targetActionUrl = targetActionUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
