package com.ccdd.message.dto;

import com.ccdd.message.entity.MessagePriority;
import com.ccdd.message.entity.MessageRootCategory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送人工内部邮件请求载荷 (对齐 OpenAPI 8.2)
 */
public class SendManualMessageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String subject;
    private String content;
    private List<RecipientItem> recipients = new ArrayList<>();
    private List<AttachmentItem> attachments = new ArrayList<>();
    private MessagePriority priority = MessagePriority.NORMAL;

    private String relatedProjectId;
    private String relatedObjType;
    private String relatedObjId;
    private String targetActionUrl;
    private Long quotedMessageId;

    // 防伪造注入防护校验：客户端显式传递系统标识将被拦截
    private MessageRootCategory rootCategory;
    private Boolean isSystemGenerated;

    public static class RecipientItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String userId;
        private String userName;
        private String recipientType = "TO";

        public RecipientItem() {}
        public RecipientItem(String userId, String userName, String recipientType) {
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

    public static class AttachmentItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String fileName;
        private Long fileSize;
        private String fileType;
        private String downloadUrl;

        public AttachmentItem() {}
        public AttachmentItem(String fileName, Long fileSize, String fileType, String downloadUrl) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.fileType = fileType;
            this.downloadUrl = downloadUrl;
        }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }

    public SendManualMessageRequest() {
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

    public List<RecipientItem> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<RecipientItem> recipients) {
        this.recipients = recipients;
    }

    public List<AttachmentItem> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentItem> attachments) {
        this.attachments = attachments;
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

    public Long getQuotedMessageId() {
        return quotedMessageId;
    }

    public void setQuotedMessageId(Long quotedMessageId) {
        this.quotedMessageId = quotedMessageId;
    }

    public MessageRootCategory getRootCategory() {
        return rootCategory;
    }

    public void setRootCategory(MessageRootCategory rootCategory) {
        this.rootCategory = rootCategory;
    }

    public Boolean getIsSystemGenerated() {
        return isSystemGenerated;
    }

    public void setIsSystemGenerated(Boolean systemGenerated) {
        isSystemGenerated = systemGenerated;
    }
}
