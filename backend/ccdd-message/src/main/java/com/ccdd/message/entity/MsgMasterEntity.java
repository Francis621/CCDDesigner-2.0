package com.ccdd.message.entity;

import java.io.Serializable;
import java.time.Instant;

/**
 * 消息主实体 (不可变投递模型，对应 plm_msg.msg_master)
 */
public class MsgMasterEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private MessageRootCategory rootCategory;
    private SystemMessageType systemType;
    private String subject;
    private String content;
    private MessagePriority priority;
    private String senderUserId;
    private String senderUserName;
    private String relatedProjectId;
    private String relatedObjType;
    private String relatedObjId;
    private String targetActionUrl;
    private String actionIdentifier;
    private String actionPayloadJson;
    private Boolean isSystemGenerated;
    private Instant createdAt;

    public MsgMasterEntity() {
    }

    public MsgMasterEntity(Long messageId, MessageRootCategory rootCategory, SystemMessageType systemType,
                           String subject, String content, MessagePriority priority,
                           String senderUserId, String senderUserName,
                           String relatedProjectId, String relatedObjType, String relatedObjId,
                           String targetActionUrl, String actionIdentifier, String actionPayloadJson,
                           Boolean isSystemGenerated, Instant createdAt) {
        this.messageId = messageId;
        this.rootCategory = rootCategory;
        this.systemType = systemType;
        this.subject = subject;
        this.content = content;
        this.priority = priority;
        this.senderUserId = senderUserId;
        this.senderUserName = senderUserName;
        this.relatedProjectId = relatedProjectId;
        this.relatedObjType = relatedObjType;
        this.relatedObjId = relatedObjId;
        this.targetActionUrl = targetActionUrl;
        this.actionIdentifier = actionIdentifier;
        this.actionPayloadJson = actionPayloadJson;
        this.isSystemGenerated = isSystemGenerated;
        this.createdAt = createdAt;
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public void setSenderUserName(String senderUserName) {
        this.senderUserName = senderUserName;
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

    public String getRelatedObjId() {
        return relatedObjId;
    }

    public void setRelatedObjId(String relatedObjId) {
        this.relatedObjId = relatedObjId;
    }

    public String getTargetActionUrl() {
        return targetActionUrl;
    }

    public void setTargetActionUrl(String targetActionUrl) {
        this.targetActionUrl = targetActionUrl;
    }

    public String getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }

    public Boolean getIsSystemGenerated() {
        return isSystemGenerated;
    }

    public void setIsSystemGenerated(Boolean systemGenerated) {
        isSystemGenerated = systemGenerated;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
