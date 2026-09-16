package com.ccdd.message.dto;

import com.ccdd.message.entity.MailboxBoxType;
import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.MessageRootCategory;
import com.ccdd.message.entity.SystemMessageType;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 邮件完整详情 DTO (包含正文、附件清单、收件人列表与工程上下文)
 */
public class MessageDetailDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long messageId;
    private Long userBoxId;
    private MessageRootCategory rootCategory;
    private SystemMessageType systemType;
    private String subject;
    private String content;
    private String senderUserId;
    private String senderUserName;
    private Boolean isSystemGenerated;
    private MessagePriority priority;

    private String relatedProjectId;
    private String relatedObjType;
    private String relatedObjId;
    private String targetActionUrl;
    private String actionIdentifier;
    private String actionPayloadJson;

    private Boolean isRead;
    private Boolean isStarred;
    private Boolean isArchived;
    private MailboxBoxType boxType;

    private List<RecipientDetail> recipients = new ArrayList<>();
    private List<AttachmentDetail> attachments = new ArrayList<>();
    private Instant createdAt;

    public static class RecipientDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        private String userId;
        private String userName;
        private String recipientType;

        public RecipientDetail() {}
        public RecipientDetail(String userId, String userName, String recipientType) {
            this.userId = userId;
            this.userName = userName;
            this.recipientType = recipientType;
        }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getRecipientType() { return recipientType; }
        public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    }

    public static class AttachmentDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long attachmentId;
        private String fileName;
        private Long fileSize;
        private String fileType;
        private String downloadUrl;

        public AttachmentDetail() {}
        public Long getAttachmentId() { return attachmentId; }
        public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    public MessageDetailDto() {
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getBodyContent() {
        return content;
    }

    public void setBodyContent(String bodyContent) {
        this.content = bodyContent;
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

    public Boolean getIsSystemGenerated() {
        return isSystemGenerated;
    }

    public void setIsSystemGenerated(Boolean systemGenerated) {
        isSystemGenerated = systemGenerated;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
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

    public MailboxBoxType getBoxType() {
        return boxType;
    }

    public void setBoxType(MailboxBoxType boxType) {
        this.boxType = boxType;
    }

    public List<RecipientDetail> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<RecipientDetail> recipients) {
        this.recipients = recipients;
    }

    public List<AttachmentDetail> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentDetail> attachments) {
        this.attachments = attachments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
